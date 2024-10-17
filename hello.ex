defmodule Hello do

def cycleSplit(source, position, step, open, low, high, close) do
	if length(source)>position do
    sub =Enum.slice(source, position, step)
	  writeList(open, low, high, close, sub)
		cycleSplit(source, position+step, step, open, low, high, close)
    else
    IO.puts "Complete!~n"
    end
end

