(ns leiningen.init-script
  (:use [clojure.contrib.pprint]
	[leiningen.uberjar]
	[clojure.contrib.duck-streams]
	))


;; Testing options
(def test-opts {:properties {:clj-config.env "dev"
			     :java.library.path "/some/dir"}
		:java-opts ["-server" 
			    "-Xms1G"
			    "-Xmx2G"
			    "-XX:MaxPermSize=128M"]})


;; Taken from c.c.string
(defn as-str
  "Like clojure.core/str, but if an argument is a keyword or symbol,
  its name will be used instead of its literal representation.

  Example:
     (str :foo :bar)     ;;=> \":foo:bar\"
     (as-str :foo :bar)  ;;=> \"foobar\" 

  Note that this does not apply to keywords or symbols nested within
  data structures; they will be rendered as with str.

  Example:
     (str {:foo :bar})     ;;=> \"{:foo :bar}\"
     (as-str {:foo :bar})  ;;=> \"{:foo :bar}\" "
  ([] "")
  ([x] (if (instance? clojure.lang.Named x)
         (name x)
         (str x)))
  ([x & ys]
     ((fn [#^StringBuilder sb more]
        (if more
          (recur (. sb  (append (as-str (first more)))) (next more))
          (str sb)))
      (new StringBuilder #^String (as-str x)) ys)))

(defn format-properties [opts]
  (if (nil? (:properties opts))
    ""
    (apply str (interpose " " (map #(as-str "\"-D" % "=" (% (:properties opts)) "\"") (keys (:properties opts)))))))

(defn format-java-opts [opts]
  (let [java-opts (:java-opts opts)]
    (apply str (interpose " " java-opts))))

(defn format-java-string [opts]
  (str (format-properties opts) " " (format-java-opts opts)))

(def gen-init-script)
(def gen-install-script)

(defn resource-input-stream [res-name]
  (.getResourceAsStream (.getContextClassLoader (Thread/currentThread)) res-name))

(def init-script-template (slurp* (resource-input-stream "init-script-template")))
(def install-template (slurp* (resource-input-stream "install-template")))
(def clean-template (slurp* (resource-input-stream "clean-template")))

(defn gen-init-script [project opts]
  (let [name (:name project)
	description (:description project)
	pid-dir (:pid-dir opts)
	jar-install-dir (:jar-install-dir opts)
	java-flags (format-java-string opts)
	redirect-output-to (:redirect-output-to opts)]
    (format init-script-template name pid-dir jar-install-dir java-flags redirect-output-to)))

(defn gen-install-script [uberjar-path init-script-path opts]
  (let [jar-install-dir (:jar-install-dir opts)
	init-script-install-dir (:init-script-install-dir opts)
	name (:name opts)
	installed-init-script-path (str init-script-install-dir "/" name "d")]
    (format install-template 
	    name 
	    jar-install-dir 
	    uberjar-path 
	    init-script-install-dir 
	    init-script-path 
	    installed-init-script-path)))

(defn gen-clean-script [project opts]
  (let [jar-install-dir (:jar-install-dir opts)
	init-script-install-dir (:init-script-install-dir opts)
	name (:name project)]
    (format clean-template name jar-install-dir init-script-install-dir)))

(defn create-output-dir [path]
  (.mkdirs (java.io.File. path)))

(defn defaults [project]
  (let [name (:name project)
	root (:root project)]
    {:name name
     :pid-dir "/var/run"
     :jar-install-dir (str "/usr/local/" name)
     :init-script-install-dir "/etc/init.d"
     :artifact-dir (str root "/init-script")
     :redirect-output-to "/dev/null"}))

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
