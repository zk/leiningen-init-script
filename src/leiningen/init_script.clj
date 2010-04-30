(ns leiningen.init-script
  (:use [clojure.contrib.pprint]
	[leiningen.uberjar]
	[clojure.contrib.duck-streams]
	))

(def gen-init-script)
(def gen-install-script)
(def gen-clean-script)

(defn create-output-dir [path]
  (.mkdirs (java.io.File. path)))

(defn defaults [project]
  (let [name (:name project)
	root (:root project)]
    {:name name
     :pid-dir "/var/run"
     :jar-install-dir (str "/usr/local/" name)
     :init-script-install-dir "/etc/init.d"
     :artifact-dir (str root "/init-script")}))

(defn init-script [projects & args]
  (let [opts (merge (defaults projects) (:lis-opts projects))
	root (:root projects)
	name (:name opts)
	artifact-dir (:artifact-dir opts)
	source-uberjar-path (str root "/" name "-standalone.jar")
	artifact-uberjar-path (str artifact-dir "/" name "-standalone.jar")
	artifact-init-script-path (str artifact-dir "/" name "d")
	install-script-path (str artifact-dir "/" "install-" name)
	clean-script-path (str artifact-dir "/" "clean-" name)]
    (create-output-dir artifact-dir)
    (uberjar projects)
    (copy (java.io.File. source-uberjar-path) (java.io.File. artifact-uberjar-path))
    (spit artifact-init-script-path (gen-init-script projects opts))
    (spit 
     install-script-path 
     (gen-install-script artifact-uberjar-path artifact-init-script-path opts))
    (spit clean-script-path (gen-clean-script projects opts))
    (println (str "*** Done generating init scripts, see the " artifact-dir " directory"))))

(defn gen-install-script [uberjar-path init-script-path opts]
  (let [jar-install-dir (:jar-install-dir opts)
	init-script-install-dir (:init-script-install-dir opts)
	name (:name opts)
	installed-init-script-path (str init-script-install-dir "/" name "d")]
    (str "#!/bin/bash
mkdir -p " jar-install-dir "
cp " uberjar-path " " jar-install-dir "/" name "-standalone.jar
mkdir -p " init-script-install-dir "
cp " init-script-path " " init-script-install-dir "
chmod u+x " installed-init-script-path "
")))

(defn gen-clean-script [project opts]
  (let [jar-install-dir (:jar-install-dir opts)
	init-script-install-dir (:init-script-install-dir opts)
	name (:name project)]
    (str "#!/bin/bash
rm -f " jar-install-dir "/" name "-standalone.jar
rm -f " init-script-install-dir "/" name "d
")))


(defn gen-init-script [project opts]
  (let [name (:name project)
	description (:description project)
	pid-dir (:pid-dir opts)
	jar-install-dir (:jar-install-dir opts)]
    (str "#!/bin/bash
#
#	/etc/rc.d/init.d/" name "d
#      
#      " name " daemon
#      " description "
#

NAME=" name "
PID_DIR=" pid-dir "
PID_FILE=$PID_DIR/$NAME.pid
INSTALL_DIR=" jar-install-dir "
INSTALL_JAR=$INSTALL_DIR/$NAME-standalone.jar
mkdir -p $PID_DIR

start() {
	if [ -e $PID_FILE ]
	then
		echo \"$NAME is already running as process `cat $PID_FILE`.\"
		exit 1
	fi
	echo \"Starting $NAME\"
	java -jar $INSTALL_JAR > /dev/null &
	PID=$!
	echo $PID > $PID_FILE
	exit 0
}	

stop() {
	if [ ! -e $PID_FILE ]
	then
		echo \"$NAME is not running.\"
	else
		echo \"Shutting down $NAME\"
		kill `cat $PID_FILE`
		rm $PID_FILE
	fi
}

status() {
	if [ -e $PID_FILE ]
	then
		echo \"$NAME is running.\"
	else
		echo \"$NAME is not running.\"
	fi
	exit 0
}

case \"$1\" in
    start)
	start
	;;
    stop)
	stop
	;;
    status)
	status
	;;
    restart)
    stop
	start
	;;
    *)
        echo \"Usage: $NAME\"\"d {start|stop|status|restart}\"
	exit 1
	;;
esac
exit $?")))