(ns leiningen.init-script
  (:use [clojure.contrib.pprint]
	[leiningen.uberjar]
	[clojure.contrib.duck-streams]
	))

(def init-script-template)
(def install-script-template)
(def clean-script-template)

(defn create-output-dir [path]
  (.mkdirs (java.io.File. path)))

(defn opts-to-map
  ([args] 
     (opts-to-map args {}))
  ([args defaults]
      (let [res (reduce #(assoc %1 (first %2) (second %2)) {} (partition 2 args))]
	(conj defaults res))))

(defn defaults [project]
  (let [name (:name project)
	root (:root project)]
    {:name name
     :pid-dir "/var/run"
     :install-dir (str "/usr/local/" name)
     :init-script-dir "/etc/init.d"}))

(defn init-script [projects & args]
  (let [root (:root projects)
	name (:name projects)
	output-dir-path (str root "/init-script/")
	uberjar-file  (java.io.File. (str root "/" (:name projects) "-standalone.jar"))
	uberjar-lsg-file (java.io.File. (str output-dir-path (:name projects) "-standalone.jar"))
	init-script-path (str output-dir-path (:name projects) "d")
	install-script-path (str output-dir-path "install-" (:name projects))
	clean-script-path (str output-dir-path "clean-" (:name projects))
	opts (merge (defaults projects) (:lsg-opts projects))]
    (create-output-dir output-dir-path)
    (uberjar projects)
    (copy uberjar-file uberjar-lsg-file)
    (spit init-script-path (init-script-template projects opts))
    (spit 
     install-script-path 
     (install-script-template (.getAbsolutePath uberjar-file) init-script-path opts))
    (spit clean-script-path (clean-script-template projects opts))
    (println (str "*** Done generating init scripts, see the " output-dir-path " directory"))))

(defn install-script-template [uberjar-path init-script-path opts]
  (let [install-dir (:install-dir opts)
	init-script-dir (:init-script-dir opts)
	name (:name opts)
	installed-init-script-path (str init-script-dir "/" name "d")]
    (str "#!/bin/bash
mkdir -p " install-dir "
cp " uberjar-path " " install-dir "/" name "-standalone.jar
mkdir -p " init-script-dir "
cp " init-script-path " " init-script-dir "
chmod u+x " installed-init-script-path "
")))

(defn clean-script-template [project opts]
  (let [install-dir (:install-dir opts)
	init-script-dir (:init-script-dir opts)
	name (:name project)]
    (str "#!/bin/bash
rm -f " install-dir "/" name "-standalone.jar
rm -f " init-script-dir "/" name "d
")))


(defn init-script-template [project opts]
  (let [name (:name project)
	description (:description project)
	pid-dir (:pid-dir opts)
	install-dir (:install-dir opts)]
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
INSTALL_DIR=" install-dir "
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
        echo 'Usage $NAMEd {start|stop|status|restart}'
	exit 1
	;;
esac
exit $?")))