# leiningen-init-script

A leiningen plugin that allows you to generate *NIX init scripts.  Alpha quality.

If you have an feature suggestions / bug reports, please open up an [issue](https://github.com/zkim/leiningen-init-script/issues)

## Lein

Add <code>[leiningen-init-script "0.1.0"]</code> to the :dev-dependencies section in your project.clj.

As of now leiningen-init-script is only supported on lein version 1.1.0, but it's on my to do list to
add support for lein HEAD.

## Usage

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
	
## Limitations




## License

[Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html)
