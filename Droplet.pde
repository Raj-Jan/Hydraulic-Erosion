class Drop //<>//
{
    PVector pos;
    PVector dir;
    float vel;
    float water;
    float sediment;
        
    static final float pInertia = 0.9;
    static final float pMinSlope = 0.05;
    static final float pCapacity = 30;
    static final float pDeposition = 0.1;
    static final float pErosion = 0.2;
    static final float pGravity = 1;
    static final float pEvaporation = 0.02;
    
    Drop()
    {
      water = 10;
      
      pos = new PVector();
      pos.x = random(1, map.mapWidth - 1);
      pos.y = random(1, map.mapHeight - 1);
      
      dir = new PVector();
    }
    
    void show()
    {
      stroke(255, 0, 0);
      strokeWeight(10);
      point(pos.x, pos.y);
    }
    
    boolean evalOnce()
    {      
       PVector g = map.grad(pos.x, pos.y);
       dir = PVector.sub(PVector.mult(dir, pInertia), PVector.mult(g, (1 - pInertia)));
       dir.setMag(1);
       
       PVector pOld = new PVector(pos.x, pos.y);
       
       pos = PVector.add(pos, dir);
         
       if(!map.contains(pos.x, pos.y))
         return false;
         
       float hOld = map.sample(pOld.x, pOld.y);
       float hNew = map.sample(pos.x, pos.y);
       
       float dh = hNew - hOld;
       
       if(dh > 0)
       {
          float v = min(dh, sediment);
          
          map.add(pOld.x, pOld.y, v);
          sediment -= v;
          
          if(sediment == 0)
            return false;
       }
       else
       {     
          float c = max(-dh, pMinSlope) * vel * water * pCapacity;

          if(sediment > c)
          {
            float v = (sediment - c) * pDeposition;
            sediment -= v;
            map.add(pOld.x, pOld.y, v);
          }
          else if (sediment < c)
          {
             float v = min((c - sediment) * pErosion, -dh);
             sediment += v;
             map.add(pOld.x, pOld.y, -v);
          }

       }
       
       vel = sqrt(vel * vel - dh * pGravity);
       water = water * (1 - pEvaporation);

       return map.contains(pos.x, pos.y) && !Double.isNaN(vel);
    }
}
