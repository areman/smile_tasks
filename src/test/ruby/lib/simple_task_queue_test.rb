require 'test/unit'

class SimpleTaskQueueTest < Test::Unit::TestCase


  def test_hallo

    queue = Smile::Tasks::SimpleTaskQueue.new( :capacity => 100, :threads => 2 )

    result_count = 0
    results = []

    error_count = 0
    errors = []

    queue.on_result do |result|
        result_count += 1
        results << result
    end

    queue.on_error do |error|
        error_count += 1
        errors << error
    end

    100.times do
        queue.submit do
            Java::java.lang.Thread.currentThread.getId
        end

    end

    queue.await()

    assert_equal 0, error_count

    assert_equal 100, result_count
    assert_equal 2, results.uniq.size

    assert_equal 0, error_count
  end

end