require File.dirname(__FILE__) + '/helper'

class TestApp < Sipatra::Base
  invite /^sip:test_uri$/ do
    block_called
  end
end

def mock_request(method, uri, headers = {})
  unless @mock_request
    @mock_request = mock('MockSipRequest')
    @mock_request.stub!(:method => method, :requestURI => uri) 
    
    @mock_request.stub!(:getHeader).and_return(nil)
    headers.each_pair { |name, value|
      @mock_request.should_receive(:getHeader).with(name.to_s).any_number_of_times.and_return(value)
    }    
  end
  @mock_request.stub!(:respond_to?).with(:getRequest).and_return(false)
  @app.msg = @mock_request
end

def mock_response(method, status_code, headers = {})
  unless @mock_response
    @mock_response = mock('MockSipResponse')
    @mock_response.stub!(:method => method, :status => status_code)
    @mock_response.stub!(:getHeader).and_return(nil)
    headers.each_pair { |name, value|
      @mock_response.should_receive(:getHeader).with(name.to_s).any_number_of_times.and_return(value)
    }    
  end
  @mock_response.stub! :getRequest # needed to decide if the message is a response or a request
  @app.msg = @mock_response
end

describe Sipatra::Base do
  [:ack, :bye, :cancel, :info, :invite, :message, 
    :notify, :options, :prack, :publish, :refer, 
    :register, :subscribe, :update, 
    :request, :response, :helpers, :before, :after].each do |name|
    it "should accept method handler '#{name}'" do
      Sipatra::Base.respond_to?(name).should be_true
      TOPLEVEL_BINDING.eval("private_methods.include? '#{name}'").should be_true
    end
  end
  
  it "passes the subclass to configure blocks" do
    ref = nil
    TestApp.configure { |app| ref = app }
    ref.should == TestApp
  end  
end

describe 'Sipatra::Base subclasses' do
  
  before do  
    @app = Class::new(Sipatra::Base).new
    @app.set_bindings nil, nil, mock('SipSessionMock'), mock_request('INVITE', 'sip:uri')
  end
  
  subject do
    @app 
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
  
  it "session[] should return a session attribute value" do
    @app.session.should_receive(:getAttribute).with('foo').twice.and_return('bar')
      
    @app.session['foo'].should == 'bar'
    @app.session[:foo].should  == 'bar'
  end

  it "session[]= should set session attribute value" do
    @app.session.should_receive(:setAttribute).with('foo', 'bar').twice
      
    @app.session['foo'] = 'bar'
    @app.session[:foo]  = 'bar'
  end
  
  it "session[]= should remove a session attribute if value is nil" do
    @app.session.should_receive(:removeAttribute).with('foo').twice
      
    @app.session['foo'] = nil
    @app.session[:foo]  = nil
  end
  
  it "msg.uri should return msg.requestUri" do
    @app.msg.uri.should == "sip:uri"
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

describe TestApp do
  
  describe "when calling do_request" do
    subject do
      @app = TestApp::new
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

describe Sipatra::Base do
  before do
    @app = Class::new(Sipatra::Base).new
  end
  
  subject { @app }
  
  describe 'params' do
  
    before do
      @app.msg = mock_request('INVITE', 'sip:+uri-1-2-3:pass@domain.com;params1=test')
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
      
        subject.params[:uri].should == %w(sip:+uri-1-2-3:pass@domain.com;params1=test +uri-1-2-3 
          pass domain.com ;params1=test params1 test)
      end
    end
  end


  describe 'params with conditions' do
  
    before do
      @app.msg = mock_request('INVITE', 'sip:user@domain.com', 
        :Header1 => 'sip:user:pass@domain.com',
        :Header2 => 'sip:user:pass@domain.com')
    end
  
    describe "when receiving do_request" do
      it "handler with conditions on Header1 and Header2 should be called" do
        subject.class.invite /sip:(.*)@(.*)/, 
          :Header1 => /sip:(.*):(.*)@(.*)/, 
          :Header2 => /sip:(.*)@(.*)/ do
          must_be_called
        end
        subject.should_receive(:must_be_called)
        subject.do_request
      
        subject.params.size.should == 3
        subject.params[:uri].should == %w(sip:user@domain.com user domain.com)
        subject.params[:Header1].should == %w(sip:user:pass@domain.com user pass domain.com)
        subject.params[:Header2].should == %w(sip:user:pass@domain.com user:pass domain.com)
        subject.params[:Header3].should == nil
      end
      
      it "handler with conditions on Header1 and Header3 should NOT be called" do
        subject.class.invite /sip:(.*)@(.*)/, 
          :Header1 => /sip:(.*):(.*)@(.*)/, 
          :Header3 => /.*/ do
          must_not_be_called
        end
        subject.should_not_receive(:must_be_called)
        subject.do_request
      end
    end
  end

  describe 'responses with params' do
  
    before do
      @app.msg = mock_response 'INVITE', 200,
        :Header1 => 'sip:user:pass@domain.com',
        :Header2 => 'sip:user:pass@domain.com'
    end
  
    it 'should have an empty size by default' do
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
        subject.class.response :INVITE, 200, 
          :Header1 => /sip:(.*):(.*)@(.*)/, 
          :Header2 => /sip:(.*)@(.*)/ do
            must_be_called
        end
        subject.should_receive(:must_be_called)
        subject.do_response
      
        subject.params.size.should == 2
        subject.params[:Header1].should == %w(sip:user:pass@domain.com user pass domain.com)
        subject.params[:Header2].should == %w(sip:user:pass@domain.com user:pass domain.com)
        subject.params[:Header3].should == nil
      end

      it "should NOT have access to params " do
        subject.class.response :INVITE, 200, 
          :Header1 => /sip:(.*):(.*)@(.*)/, 
          :Header2 => /sip:(.*)@(.*)/, 
          :Header3 => /tel:(.*)/ do
            must_not_be_called
        end
        subject.should_not_receive(:must_not_be_called)
        subject.do_response
      end
    
      it "should have access to params when no status nor method are set " do
        subject.class.response(:Header1 => /sip:(.*):(.*)@.*/, 
          :Header2 => /sip:(.*)@.*/) do
          must_be_called
        end
        subject.should_receive(:must_be_called)
        subject.do_response
      
        subject.params.size.should == 2
        subject.params[:Header1].size.should == 3
        subject.params[:Header2].size.should == 2
        subject.params[:Header3].should == nil
      end
    
      it "should have access to params only status is set " do
        subject.class.response(200, :Header1 => /sip:(.*):(.*)@.*/, :Header2 => /sip:(.*)@(.*)/) do
          must_be_called
        end
        subject.should_receive(:must_be_called)
        subject.do_response
      
        subject.params.size.should == 2
        subject.params[:Header1].size.should == 3
        subject.params[:Header2].size.should == 3
      end
    end  
  end
  
  describe "#before" do
    it "should add a before filter" do
      subject.class.before { before_call }
      
      subject.should_receive(:before_call).exactly(2)
      subject.class.after_filters.should == []
      subject.class.before_filters.size.should == 1
      mock_request('INVITE', 'sip:uri')
      subject.do_request
      mock_response('INVITE', 200)
      subject.do_response
    end
    
    it "should add a before request filter" do
      subject.class.before :request do before_call end
      
      subject.should_receive(:before_call).exactly(2)
      subject.class.after_filters.should == []
      subject.class.before_filters.size.should == 1
      mock_request('INVITE', 'sip:uri')
      subject.do_request
      subject.do_request
      mock_response('INVITE', 200)
      subject.do_response
    end

    it "should add a before response filter" do
      subject.class.before :response do before_call end
      
      subject.should_receive(:before_call).exactly(2)
      subject.class.after_filters.should == []
      subject.class.before_filters.size.should == 1
      mock_request('INVITE', 'sip:uri')
      subject.do_request
      mock_response('INVITE', 200)
      subject.do_response
      subject.do_response
    end    
  end
  
  describe "#after" do
    it "should add an after filter" do
      subject.class.after { after_call }
      
      subject.should_receive(:after_call).exactly(2)
      subject.class.before_filters.should == []
      subject.class.after_filters.size.should == 1
      mock_request('INVITE', 'sip:uri')
      subject.do_request
      mock_response('INVITE', 200)
      subject.do_response
    end

    it "should add an after request filter" do
      subject.class.after :request do after_call end
      
      subject.should_receive(:after_call).exactly(2)
      subject.class.before_filters.should == []
      subject.class.after_filters.size.should == 1
      mock_request('INVITE', 'sip:uri')
      subject.do_request
      subject.do_request
      mock_response('INVITE', 200)
      subject.do_response
    end

    it "should add an after response filter" do
      subject.class.after :response do after_call end
      
      subject.should_receive(:after_call).exactly(2)
      subject.class.before_filters.should == []
      subject.class.after_filters.size.should == 1
      mock_request('INVITE', 'sip:uri')
      subject.do_request
      mock_response('INVITE', 200)
      subject.do_response
      subject.do_response

    end    
  end
  
end
