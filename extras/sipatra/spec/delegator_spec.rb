require File.dirname(__FILE__) + '/helper'

describe Object do
  it "should add a request handler to Sipatra::Application" do
    invite(/sip:new_uri/) {}
    
    Sipatra::Application.instance_variable_get(:@req_handlers)['INVITE'].size.should == 1
  end

  it "should add a request handler to Sipatra::Application" do
    response :invite , /sip:new_uri/ do end
    
    Sipatra::Application.instance_variable_get(:@resp_handlers)['INVITE'].size.should == 1
  end

  describe "#helpers" do
    module TestModule
      def a_helper
      end
    end
    
    it "should add helpers defined in the block" do
      helpers do
        def a_helper
          # TODO
        end
      end
  
      Sipatra::Application.public_methods.find(:a_helper).should_not be_nil
    end
    
    it "should add helpers defined in the module" do
      helpers TestModule
  
      Sipatra::Application.public_methods.find(:a_helper).should_not be_nil
    end
    
  end
end
