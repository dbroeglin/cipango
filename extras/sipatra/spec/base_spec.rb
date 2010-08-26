require File.dirname(__FILE__) + '/helper'

class TestApp < Sipatra::Base
  invite /^sip:test_uri$/ do
    block_called
  end
end

def mock_request(method, uri)
  unless @mock_request
    @mock_request = mock('MockSipRequest')
    @mock_request.should_receive(:method).any_number_of_times.and_return(method)
    @mock_request.should_receive(:requestURI).any_number_of_times.and_return(uri)
  end
  @mock_request
end

def mock_response
  @mock_response ||= mock('SipServletResponse')
end

describe 'Sipatra::Base subclasses' do

  subject do
    app_class = Class::new(Sipatra::Base)
    app = app_class.new
    app.message = mock_request('INVITE', 'sip:uri')
    app
  end

  it 'processes requests with do_request' do
    subject.respond_to?(:do_request).should be_true
  end
  
  it 'processes responses with do_response' do
    subject.respond_to?(:do_response).should be_true
  end

  describe "when receiving do_request (with URI sip:uri)" do
    after do
      subject.do_request
    end

    it "should pass processing to the next matching handler" do
      subject.class.invite(/sip:uri/) do
        must_be_called1
        pass
        must_not_be_called
      end
      subject.class.invite(/sip:uri/) do
        must_be_called2
      end
    
      subject.should_receive(:must_be_called1)
      subject.should_not_receive(:must_not_be_called)
      subject.should_receive(:must_be_called2)
    end

    it "should stop processing" do
      subject.class.invite(/sip:uri/) do
        must_be_called
        halt
        must_not_be_called1
      end
      subject.class.invite(/sip:uri/) do
        must_not_be_called2
      end
    
      subject.should_receive(:must_be_called)
      subject.should_not_receive(:must_not_be_called1)
      subject.should_not_receive(:must_not_be_called2)
    end
  end
end

describe 'Sipatra::Base should have handlers for SIP request methods' do
  [:ack, :bye, :cancel, :info, :invite, :message, 
    :notify, :options, :prack, :publish, :refer, 
    :register, :subscribe, :update, :request].each do |name|
    it "should accept method handler #{name}" do
      Sipatra::Base.respond_to?(name).should be_true
    end
  end
  
  it "passes the subclass to configure blocks" do
    ref = nil
    TestApp.configure { |app| ref = app }
     ref.should == TestApp
  end   
end

describe TestApp do
  
  describe "when calling do_request" do
    subject do
      TestApp::new
    end
  
    after(:each) do
      subject.do_request
    end
  
    it "should invoke the handler" do
      subject.message = mock_request('INVITE', 'sip:test_uri')

      subject.should_receive(:block_called)
    end
  
    it "should not invoke the handler" do
      subject.message = mock_request('INVITE', 'sip:wrong_test_uri')

      subject.should_not_receive(:block_called)
    end
  end
end

describe TestApp do
  it "should add a request handler" do
    TestApp.invite(/sip:new_uri/) {}
    TestApp.register(/sip:new_uri/) {}
    
    TestApp.instance_variable_get(:@req_handlers)['INVITE'].size.should == 2
    TestApp.instance_variable_get(:@req_handlers)['REGISTER'].size.should == 1
  end

  it "should add a response handler" do
    TestApp.response(:invite, /sip:new_uri/) {}
    
    TestApp.instance_variable_get(:@resp_handlers)['INVITE'].size.should == 1
  end
  
  it "should add a default request handler" do
    TestApp.request {}
    TestApp.instance_variable_get(:@req_handlers)['_'].size.should == 1
  end

  it "should add a default response handler" do
    TestApp.response {}
    TestApp.instance_variable_get(:@resp_handlers)['_'].size.should == 1
  end
end