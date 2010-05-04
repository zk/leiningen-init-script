# leiningen-init-script

A leiningen plugin that generates *NIX init scripts.  Alpha quality, verified working on OSX 10.6 and FC12.

In a nutshell, LSI generates the following artifacts which can be found in your <project-root>/init-script dir:

* Project Uberjar
<br />

* <your-project-name>d script
	
	Paired down from the Fedora Core init script template, injected with values from your lein project.clj.
<br />

* install-<your-project-name> script
	
	Creates (but does not overwrite) the :pid-dir, :install-dir, and :init-script-dir directories.  To override the defaults see the Configuration section below.
<br />

* clean-<your-project-name> script 

	Removes the init script, and uberjar from their respective install paths.  Does not remove any created directories.
<br />

If you have an feature suggestions / bug reports, please open up an [issue](https://github.com/zkim/leiningen-init-script/issues)

## Why?

Because it was too damn time-consuming to turn a java program into a *nix daemon service that can be started / stopped asyncronously, chkconfig'd, etc.

## Lein

Add <code>[leiningen-init-script "0.1.0"]</code> to the :dev-dependencies section in your project.clj.

As of now leiningen-init-script is only supported on lein version 1.1.0, but it's on my to do list to
add support for lein HEAD.

## Usage

### Short Version
Create a main class for your project, run <code>lein init-script</code>, and check the ./init-script directory.


### Long Version
#### Taken from the [init-script-test](http://github.com/zkim/init-script-test) project.

Clone the init-script-test repo

    git clone git://github.com/zkim/init-script-test.git

cd into the cloned repo directory

	cd init-script-test</code>
	
Download dependencies
	
	lein deps
	
Run the init-script task
	
	lein init-script
	
	Your output should look something like:
	
	Created /Users/zkim/tmp/init-script-test/init-script-test.jar
	Including init-script-test.jar
	Including clojure-1.1.0.jar
	Including clojure-contrib-1.1.0.jar
	Including leiningen-init-script-0.1.0.jar
	*** Done generating init scripts, see the /Users/zkim/tmp/init-script-test/init-script/ directory
	napple:init-script-test zkim$
	
cd into the init-script directory
	cd ./init-script
	
Make install-init-script-test, clean-init-script-test runnable
	chmod u+x ./install-init-script-test
	chmod u+x ./clean-init-script-test
	
Install init script and jar
	sudo ./install-init-script-test
	
leiningen-init-script installs the jar to /usr/local/<project-name> and the init script to /etc/init.d. These defaults can be changed, see the Configuration section of the [leiningen-init-script](http://github.com/zkim/leiningen-init-script) README


Start the daemon service
	sudo /etc/init.d/init-script-testd start
	
Verify the jar is running
	ps -e
	
	Output:
	
	44678 ttys003    0:00.01 login -pf zkim
	44679 ttys003    0:00.11 -bash
	45216 ttys003    0:01.28 /usr/bin/java -jar /usr/local/init-script-test/init-script-test-standalone.jar
	45225 ttys003    0:00.00 ps -e
	
Stop the daemon service and verify the process has stopped
	sudo /etc/init.d/init-script-testd stop
	
	ps -e
	
	44198 ttys002    0:00.09 -bash
	44678 ttys003    0:00.01 login -pf zkim
	44679 ttys003    0:00.11 -bash
	45248 ttys003    0:00.00 ps -e

## Configuration

leiningen-init-script takes several options in the form of:

	{:name "override-project-name"
     :pid-dir "/var/run"
     :install-dir "/usr/local/my-project-name"
     :init-script-dir "/etc/init.d"}

which are passed to the the init-script task by adding a :lis-opts entry to the project map. For example:

	(defproject init-script-test "0.1.0"
	  :description "Test project for leiningen-init-script"
	  :dependencies [[org.clojure/clojure "1.1.0"]
	                 [org.clojure/clojure-contrib "1.1.0"]]
	  :dev-dependencies [[leiningen-init-script "0.1.0"]]
	  :lis-opts {:properties {:clj-config.env "dev"
				  :java.library.path "/some/dir"
				  :init.script.test.prop "test with spaces"}
		         :java-opts ["-server"
							 "-Xms256M"
				 			 "-Xmx512M"
				 			 "-XX:MaxPermSize=128M"]}
	  :main main)
	
	
## Limitations

No Windows support at this time, if you'd like to see support for windows services, please open up an issue.

## License

[Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html)


