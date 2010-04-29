# leiningen-init-script

A leiningen plugin that allows you to generate *NIX init scripts.

## Usage

Add <code>[leiningen-init-script "0.1.0"]</code> to the :dev-dependencies section in your project.clj.

## Configuration

leiningen-init-script takes several options in the form of:

	{:name "override-project-name"
     :pid-dir "/var/run"
     :install-dir "/usr/local/my-project-name"
     :init-script-dir "/etc/init.d"}

which are passed to the the init-script task by adding a :lsg-opts entry to the project map. For example:

	(defproject init-script-test "0.1.0"
	  :description "Test project for leiningen-init-script"
	  :dependencies [[org.clojure/clojure "1.1.0"]
	                 [org.clojure/clojure-contrib "1.1.0"]]
	  :dev-dependencies [[leiningen-init-script "0.1.0"]]
	  :lsg-opts {:pid-dir "/tmp/pids"
		     :install-dir "/tmp/jars"}
	  :main main)


## License

[Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html)
