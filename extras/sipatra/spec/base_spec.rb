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
    app.msg = mock_request('INVITE', 'sip:uri')
    app
  end

  it 'processes requests with do_request' do
    subject.respond_to?(:do_request).should be_true
  end
  
  it 'processes responses with do_response' do
    subject.respond_to?(:do_response).should be_true
  end

  describe '#helpers' do
    it "adds a helper method" do
      subject.class.helpers do
        def a_helper
          a_helper_body
        end
      end
      
      subject.respond_to? :a_helper
    end
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

describe Sipatra::Base do
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
      subject.msg = mock_request('INVITE', 'sip:test_uri')

      subject.should_receive(:block_called)
    end
  
    it "should not invoke the handler" do
      subject.msg = mock_request('INVITE', 'sip:wrong_test_uri')

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

describe 'Sipatra::Base params ' do

  subject do
    app_class = Class::new(Sipatra::Base)
    app = app_class.new
    app.msg = mock_request('INVITE', 'sip:+uri-1-2-3:pass@domain.com;params1=test')
    app
  end
  
  it 'should not have an empty size by default' do
    subject.params.size.should == 0
  end
  
  describe "when receiving do_request (with URI sip:+uri-1-2-3:pass@domain.com;params1=test)" do
    after do
      subject.do_request
    end
    
    it "should pass processing through a right matching string " do
      subject.class.invite('sip:(:uri):(:pass)@(:domain)') do
        must_be_called
      end
      
      subject.should_receive(:must_be_called)
    end
    
    it "should pass processing through a right regexp " do
      subject.class.invite(/sip:.*:.*@.*;.*/) do
        must_be_called
      end
      
      subject.should_receive(:must_be_called)
    end
    
     it "should not be processed through a wrong regexp " do
      subject.class.invite('sip:domain.com') do
        must_not_be_called
      end
      
      subject.should_not_receive(:must_not_be_called)
    end
  end
  
  describe "when receiving do_request" do
   it "should have access to params between brackets" do
      subject.class.invite('sip:(:user):(:pass)?@(:domain)') do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_request
        
      subject.params[:user].should == "+uri-1-2-3"
      subject.params[:pass].should == "pass"
      subject.params[:domain].should == "domain.com;params1=test"
      subject.params[:uri].should == nil
    end
  end
  
  describe "when receiving do_request" do
   it "should have access to params " do
      subject.class.invite(/sip:(.*):(.*)@([^;]*)(;([^;=]*)=([^;=]*))?/) do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_request
        
      subject.params[:uri][0].should == "sip:+uri-1-2-3:pass@domain.com;params1=test"
      subject.params[:uri][1].should == "+uri-1-2-3"
      subject.params[:uri][2].should == "pass"
      subject.params[:uri][3].should == "domain.com"
      subject.params[:uri][4].should == ";params1=test"
      subject.params[:uri][5].should == "params1"
      subject.params[:uri][6].should == "test"
      subject.params[:uri][7].should == nil
    end
  end
end