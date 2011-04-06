def sudo!(*commands)
    env = Vagrant::Environment::new
    env.primary_vm.ssh.execute do |ssh|
        ssh.sudo! commands do |ch, type, data|
            if type == :exit_status
                ssh.check_exit_status(data, commands)
            else
                puts data
                # TODO: env.ui.info(data)
            end
        end
    end
end

def exec!(*commands)
    env = Vagrant::Environment::new
    env.primary_vm.ssh.execute do |ssh|
        ssh.exec! commands
    end
end

require 'vagrant'
namespace :test do
  namespace :cipango do
    desc "Start Cipango test server"
    task :start do
        exec! "cd /cipango; PATH=/sbin:$PATH ./bin/jetty.sh start"
    end

    desc "Stop Cipango test server"
    task :stop do
        exec! "cd /cipango; ./bin/jetty.sh stop"
    end
  end
end

