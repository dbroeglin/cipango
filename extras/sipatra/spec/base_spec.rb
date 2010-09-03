require File.dirname(__FILE__) + '/helper'

class TestApp < Sipatra::Base
  invite /^sip:test_uri$/ do
    block_called
  end
end

def mock_request(method, uri, header_value = nil)
  unless @mock_request
    @mock_request = mock('MockSipRequest')
    @mock_request.should_receive(:method).any_number_of_times.and_return(method)
    @mock_request.should_receive(:requestURI).any_number_of_times.and_return(uri)
    if !header_value.nil?
      @mock_request.should_receive(:addHeader).any_number_of_times
      @mock_request.should_receive(:getHeader).any_number_of_times.and_return(header_value)
    end
  end
  @mock_request
end

def mock_response(method, code, header_value = nil)
  unless @mock_response
    @mock_response = mock('MockSipResponse')
    @mock_response.should_receive(:method).any_number_of_times.and_return(method)
    @mock_response.should_receive(:status).any_number_of_times.and_return(code)
    if !header_value.nil?
      @mock_response.should_receive(:addHeader).any_number_of_times
      @mock_response.should_receive(:getHeader).any_number_of_times.and_return(header_value)
    end
  end
  @mock_response
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
      subject.class.invite('sip:(:uri):(:pass)@(:domain);.*') do
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
      subject.class.invite('sip:(:user):(:pass)?@(:domain);.*') do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_request
      
      subject.params[:user].should == "+uri-1-2-3"
      subject.params[:pass].should == "pass"
      subject.params[:domain].should == "domain.com"
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


describe 'Sipatra::Base params with conditions ' do
  
  subject do
    app_class = Class::new(Sipatra::Base)
    app = app_class.new
    app.msg = mock_request('INVITE', 'sip:user@domain.com', 'sip:user:pass@domain.com')
    app
  end
  
  describe "when receiving do_request" do
    it "should have access to params with conditions " do
      subject.class.invite(/sip:(.*)@(.*)/, :Header => /sip:(.*):(.*)@(.*)/, :Header2 => /sip:(.*)@(.*)/, :Header3 => /tel:(.*)/) do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_request
      
      subject.params.size.should == 3
      subject.params[:Header].size.should == 4
      subject.params[:Header2].size.should == 3
      subject.params[:Header3].should == nil
      subject.params[:uri][1].should == "user"
      subject.params[:Header][0].should == "sip:user:pass@domain.com"
      subject.params[:Header][1].should == "user"
      subject.params[:Header][2].should == "pass"
      subject.params[:Header][3].should == "domain.com"
      subject.params[:Header2][0].should == "sip:user:pass@domain.com"
      subject.params[:Header2][1].should == "user:pass"
      subject.params[:Header2][2].should == "domain.com"
    end
  end
end

describe 'Sipatra::Base responses with params' do
  
  subject do
    app_class = Class::new(Sipatra::Base)
    app = app_class.new
    app.msg = mock_response('INVITE', 200, 'sip:user:pass@domain.com')
    app
  end
  
  it 'should not have an empty size by default' do
    subject.params.size.should == 0
  end
  
  describe "when receiving do_response" do
    after do
      subject.do_response
    end
    
    it "should pass processing through a response line " do
      subject.class.response do
        must_be_called
      end
      
      subject.should_receive(:must_be_called)
    end
    
    it "should pass processing through a response line with the right method " do
      subject.class.response(:INVITE) do
        must_be_called
      end
      
      subject.should_receive(:must_be_called)
    end
    
    it "should not pass processing through a response line with the wrong method " do
      subject.class.response(:REGISTER) do
        must_not_be_called
      end
      
      subject.should_not_receive(:must_not_be_called)
    end
    
    it "should pass processing through a response line with the right status code " do
      subject.class.response(:INVITE, 200) do
        must_be_called
      end
      
      subject.should_receive(:must_be_called)
    end
    
     it "should not pass processing through a response line with a wrong status code " do
      subject.class.response(:INVITE, 400) do
        must_not_be_called
      end
      
      subject.should_not_receive(:must_not_be_called)
    end
    
     it "should pass processing through a response line with the right status code and no method " do
      subject.class.response(200) do
        must_be_called
      end
      
      subject.should_receive(:must_be_called)
    end
    
    it "should pass processing through a response line with the wrong status code and no method " do
      subject.class.response(600) do
        must_not_be_called
      end
      
      subject.should_not_receive(:must_not_be_called)
    end
    
    it "should pass processing through a response line with the wrong status code and no method " do
      subject.class.response(300, :Header => /sip:(.*):(.*)@(.*)/) do
        must_not_be_called
      end
      
      subject.should_not_receive(:must_not_be_called)
    end
  end
  
  describe "when receiving do_response" do
    it "should have access to params " do
      subject.class.response(:INVITE, 200, :Header => /sip:(.*):(.*)@(.*)/, :Header2 => /sip:(.*)@(.*)/, :Header3 => /tel:(.*)/) do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_response
      
      subject.params.size.should == 2
      subject.params[:Header].size.should == 4
      subject.params[:Header2].size.should == 3
      subject.params[:Header3].should == nil
      subject.params[:Header][0].should == "sip:user:pass@domain.com"
      subject.params[:Header][1].should == "user"
      subject.params[:Header][2].should == "pass"
      subject.params[:Header][3].should == "domain.com"
      subject.params[:Header2][0].should == "sip:user:pass@domain.com"
      subject.params[:Header2][1].should == "user:pass"
      subject.params[:Header2][2].should == "domain.com"
    end
    
    it "should have access to params when no status nor method are set " do
      subject.class.response(:Header => /sip:(.*):(.*)@.*/, :Header2 => /sip:(.*)@.*/, :Header3 => /tel:(.*)/) do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_response
      
      subject.params.size.should == 2
      subject.params[:Header].size.should == 3
      subject.params[:Header2].size.should == 2
      subject.params[:Header3].should == nil
    end
    
    it "should have access to params only status is set " do
      subject.class.response(200, :Header => /sip:(.*):(.*)@.*/, :Header4 => /sip:(.*)@(.*)/) do
        must_be_called
      end
      subject.should_receive(:must_be_called)
      subject.do_response
      
      subject.params.size.should == 2
      subject.params[:Header].size.should == 3
      subject.params[:Header4].size.should == 3
    end
  end
end