require 'rake/clean'
require 'fileutils'
require 'spec/rake/spectask'

desc "Run all specs"
Spec::Rake::SpecTask.new('spec') do |t|
  t.libs = ['lib']
  t.spec_files = FileList['spec/**/*_spec.rb']
  t.verbose = true
end