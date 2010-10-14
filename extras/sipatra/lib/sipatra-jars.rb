module SipatraJars
  PATH = File.expand_path(File.dirname(__FILE__))
  puts PATH
  puts Dir[PATH + "/sipatra/*"].to_a.inspect
  JarPath = Dir[PATH + "/sipatra/*"].find { |f| f =~ %r{/sipatra-.*\.jar$} }

  class << self 
    def all_jar_names
      @all_jar_names ||= [File::basename(JarPath)]
    end
    
    def all_jar_paths
      @all_jar_paths ||= [JarPath]
    end
  end
end
