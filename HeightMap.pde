class HeightMap
{
    int mapWidth;
    int mapHeight;
    float map[];
 
    static final float pRadius = 16;
    static final float erosionFactor = 1;
 
    HeightMap(int pWidth, int pHeight, float scale)
    {
      mapWidth = pWidth;
      mapHeight = pHeight;
      map = new float[pWidth * pHeight];
      
      for (int i = 0; i < pWidth; i++)
      {
        for (int j = 0; j < pHeight; j++)
        {
          float x = scale * (float)i;
          float y = scale * (float)j;
    
          float val1 = 8 * noise.eval(x, y);
          float val2 = 4 * noise.eval(x * 2, y * 2);
          float val3 = 2 * noise.eval(x * 4, y * 4);
          float val4 = 1 * noise.eval(x * 8, y * 8);
             
          float val = (val1 + val2 + val3 + val4) / 15;
          
          map[i + j * pWidth] = val;
        }
      }
    }
 
    void show(float scale)
    {            
      noStroke();
    
      float x = 0;
      float y = 0;
      
      for (int i = 0; i < mapWidth; i++)
      {
        x = 0;
        
        for (int j = 0; j < mapHeight; j++)
        {         
          float val = map[i + mapWidth * j];
          int col = (int)(255 * (val + 1) * 0.5);

          fill(col);
          rect(x, y, scale, scale);
          
          x += scale;
        }
        
        y += scale;
      }
    }
    
    float sample(float x, float y)
    {  
       int x0 = (int)x;
       int y0 = (int)y;
       
       float dx = x - x0;
       float dy = y - y0;
       
       float h00 = map[x0 + mapWidth * y0];

       if (dx > 0)
       {
         int x1 = x0 + 1;
         
         float h10 = map[x1 + mapWidth * y0];
         
         float h0 = h00 * (1 - dx) + h10 * dx;
         
         if (dy > 0)
         {           
           int y1 = y0 + 1;
           
           float h01 = map[x0 + mapWidth * y1];
           float h11 = map[x1 + mapWidth * y1];
           
           float h1 = h01 * (1 - dx) + h11 * dx;

           return h0 * (1 - dy) + h1 * dy;
         }
         else
         {
           return h0;
         }
       }
       else if (dy > 0)
       {
         int y1 = y0 + 1;
         
         float h01 = map[x0 + mapWidth * y1];
         
         return h01 * (1 - dy) + h00 * dy;
       }
       else
       {
         return h00;
       }
    }
    
    PVector grad(float x, float y)
    {
      int x0 = (int)x;
      int y0 = (int)y;
      
      float dx = x - x0;
      float dy = y - y0;
      
      int x1 = x0 + 1;
      int y1 = y0 + 1;
      
      float h00 = map[x0 + mapWidth * y0];
      float h10 = map[x1 + mapWidth * y0];
      float h01 = map[x0 + mapWidth * y1];
      float h11 = map[x1 + mapWidth * y1];

      float gx = 0;
      float gy = 0;

      if (dy > 0)
      {
        gx = (h10 - h00) * (1 - dy) + (h11 - h01) * dy;
      }
      if (dx > 0)
      {
        gy = (h01 - h00) * (1 - dx) + (h11 - h10) * dx;
      }

      return new PVector(gx, gy);
    }
 
    void add(float x, float y, float value)
    {     
      value *= erosionFactor;
    
      int _x = (int)x;
      int _y = (int)y;

      int xMin = max(0, (int)(x - pRadius));
      int xMax = min(mapWidth - 1, (int)(x + pRadius ) + 1);
      
      int yMin = max(0, (int)(y - pRadius));
      int yMax = min(mapHeight - 1, (int)(y + pRadius) + 1);
      
      int w = (xMax - xMin);
      int h = (yMax - yMin);

      float weights[] = new float[w * h];
      float sum = 0;
      
      for(int i = 0; i < w; i++)
      {
        for(int j = 0; j < h; j++)
        {
           int x0 = xMin + i;
           int y0 = yMin + j;
           
           float dist = (x - x0) * (x - x0) + (y - y0) * (y - y0);
           
           if(dist > pRadius * pRadius)
             continue;
           
           int index = i + j * w;
           float weight = max(0, pRadius - sqrt(dist));
           
           weights[index] = weight;
           sum += weight;
        }
      }
            
      for(int i = 0; i < w; i++)
      {
        for(int j = 0; j < h; j++)
        {
          
           int index0 = i + j * w;
           int index1 = (xMin + i) + (yMin + j) * mapWidth;
           
           map[index1] += value * weights[index0] / sum;
        }
      }
    }
    
    boolean contains(float x, float y)
    {
      return x < mapWidth - 1 && x > 1 && y < mapHeight - 1 && y > 1; 
    }
}
