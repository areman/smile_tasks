module Smile

    module Tasks

    end

end

if defined?( RUBY_ENGINE ) && RUBY_ENGINE == 'jruby'

   Dir.entries( File.dirname( __FILE__ ) ).each do |file|
   
       if file.end_with?('.jar')
          require file
       end

   end

  Java::smile.tasks.jruby.JRubySimpleTaskQueue.define( JRuby.runtime )

  Smile::Tasks::SimpleTaskQueue
end
