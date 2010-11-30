module SipatraJars
  PATH = File.expand_path(File.dirname(__FILE__))

  class << self
    def all_jar_names
      @all_jar_names ||= all_jar_paths.map { |path| File::basename(path) }
    end

    def all_jar_paths
      @all_jar_paths ||= Dir[PATH + "/sipatra/*.jar"]
    end
  end
end
