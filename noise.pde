OpenSimplexNoise noise;
HeightMap map;

void setup()
{
  size(512, 512);
  
  noise = new OpenSimplexNoise(0);
  
  map = new HeightMap(512, 512, 0.01);
  
  thread("Sim");
}

void Sim()
{
  int k = -1;
  
  while (true)
  {
    if (++k % 4000 == 0)
      println(k);
    
    Drop drop = new Drop();
    
    for (int i = 0; i < 200; i++)
    {
      if (!drop.evalOnce())
        break;
    }
  }
}

void draw()
{
  map.show(1);
}
