if defined?( RUBY_ENGINE ) && RUBY_ENGINE == 'jruby'

   Dir.entries( File.dirname( __FILE__ ) ).each do |file|
   
   if file.end_with?('.jar')
      require file
   end

   end

  Java::smile.SmileTasksService.new.basicLoad( JRuby.runtime )

end
