$top_level = self 

$LOAD_PATH << "src/main/resources"
require 'sipatra'

share_as :ExtensionsMethods do
  describe "#helpers" do
    module TestModule1
      def helper1
      end
    end

    module TestModule2
      def helper2
      end
    end
      
    it "should add helpers defined in the block" do
      subject.send :helpers do
        def a_helper
          # TODO
        end
      end
  
      Sipatra::Application.instance_methods.include?('a_helper').should be_true
    end
    
    it "should add helpers defined in the module" do
      subject.send :helpers, TestModule1, TestModule2
  
      Sipatra::Application.instance_methods.include?('helper1').should be_true
      Sipatra::Application.instance_methods.include?('helper2').should be_true
    end

    it "should add helpers defined in the module and the block" do
      subject.send :helpers, TestModule1, TestModule2 do 
        def helper3
        end
      end
  
      Sipatra::Application.instance_methods.include?('helper1').should be_true
      Sipatra::Application.instance_methods.include?('helper2').should be_true
      Sipatra::Application.instance_methods.include?('helper3').should be_true
    end    
  end
end
