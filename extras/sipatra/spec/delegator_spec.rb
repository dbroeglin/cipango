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
end
