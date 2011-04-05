Vagrant::Config.run do |config|
  config.vm.box = "squeeze64"
  
  config.vm.customize do |vm| 
    vm.memory_size = 512
  end

  # config.vm.boot_mode = :gui

  # config.vm.network "33.33.33.10"

  config.vm.forward_port "http", 8080, 8080

  config.vm.share_folder "cipango", "/cipango", "../../../cipango-distribution-2.0.0"

  config.vm.provision :chef_solo do |chef|
    chef.cookbooks_path = "cookbooks"
    chef.add_recipe "java"
    chef.add_recipe "sipp"
    chef.add_recipe "asterisk"
    chef.json.merge!( 
      :java => {
        :install_flavor => "sun"
      } 
    )
  end
end
