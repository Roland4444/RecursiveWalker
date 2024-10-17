defmodule Hello do
def applyaddsub(outs, delta_Const, delta_mult)  do
	a1 = :rand.uniform()
	generate = :rand.uniform()
  cond  do
    a1> 0.5-> outs + generate / delta_Const
    a1< 0.5-> outs - generate / delta_Const
  end
end

def cycleSplit(source, position, step, open, low, high, close) do
	if length(source)>position do
    sub =Enum.slice(source, position, step)
	  writeList(open, low, high, close, sub)
		cycleSplit(source, position+step, step, open, low, high, close)
    else
    IO.puts "Complete!~n"
    end
end

def writeList(open1, low1, high1, close1, lst) do
	[_open | _] = lst
  {min_,max_} = Enum.min_max(lst)
  IO.binwrite open1, "#{_open}\n"
  IO.binwrite low1, "#{min_}\n"
  IO.binwrite high1, "#{max_}\n"
  IO.binwrite close1, "#{List.last(lst)}\n"
end

def main do
  start = Time.utc_now()
  delta = 300.0
  step = 150
  iterations = 8*864000#00#, %%%224*8640000,%%%8640000
  blocks = 1048576

  delta_mult = 10000.0
  numberBlocks = (iterations / step /blocks + 1)

  outz=75.0
  rez=pushone([], 0, iterations, outz, delta, delta_mult)

  IO.puts "SIZE::#{length(rez)}"
  {:ok, low} = File.open "low.txt", [:write]
  {:ok, open} = File.open "open.txt", [:write]
  {:ok, high} = File.open "high.txt", [:write]
  {:ok, close} = File.open "close.txt", [:write]
  cycleSplit(Enum.reverse(rez), 1, step, open, low, high, close)
  endt = Time.utc_now()
  diffz=Time.diff(endt, start, :second)
  IO.puts "Elapsed#{diffz}"
  end
end


