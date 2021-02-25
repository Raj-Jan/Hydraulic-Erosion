public class OpenSimplexNoise {

  private static final float STRETCH_CONSTANT_2D = -0.211324865405187f;    // (1/Math.sqrt(2+1)-1)/2;
  private static final float SQUISH_CONSTANT_2D = 0.366025403784439f;      // (Math.sqrt(2+1)-1)/2;

  private static final long DEFAULT_SEED = 0;
  
  private static final int PSIZE = 2048;
  private static final int PMASK = 2047;

  private short[] perm;
  
  public OpenSimplexNoise() 
  {
    this(DEFAULT_SEED);
  }
  
  public OpenSimplexNoise(long seed) 
  {
    perm = new short[PSIZE];
    
    short[] source = new short[PSIZE]; 
    
    for (short i = 0; i < PSIZE; i++)
      source[i] = i;
      
    for (int i = PSIZE - 1; i >= 0; i--) 
    {
      seed = seed * 6364136223846793005L + 1442695040888963407L;
      int r = (int)((seed + 31) % (i + 1));
      
      if (r < 0)
        r += (i + 1);
        
      perm[i] = source[r];
      source[r] = source[i];
    }
  }
  
  public float eval(float x, float y) 
  {
    // Place input coordinates onto grid.
    float stretchOffset = (x + y) * STRETCH_CONSTANT_2D;
    float xs = x + stretchOffset;
    float ys = y + stretchOffset;
    
    // Floor to get grid coordinates of rhombus (stretched square) super-cell origin.
    int xsb = fastFloor(xs);
    int ysb = fastFloor(ys);
    
    // Compute grid coordinates relative to rhombus origin.
    float xins = xs - xsb;
    float yins = ys - ysb;
    
    // Sum those together to get a value that determines which region we're in.
    float inSum = xins + yins;

    // Positions relative to origin point.
    float squishOffsetIns = inSum * SQUISH_CONSTANT_2D;
    float dx0 = xins + squishOffsetIns;
    float dy0 = yins + squishOffsetIns;
    
    // We'll be defining these inside the next block and using them afterwards.
    float dx_ext, dy_ext;
    int xsv_ext, ysv_ext;
    
    float value = 0;

    // Contribution (1,0)
    float dx1 = dx0 - 1 - SQUISH_CONSTANT_2D;
    float dy1 = dy0 - 0 - SQUISH_CONSTANT_2D;
    float attn1 = 2 - dx1 * dx1 - dy1 * dy1;
    
    if (attn1 > 0) 
    {
      attn1 *= attn1;
      value += attn1 * attn1 * extrapolate(xsb + 1, ysb + 0, dx1, dy1);
    }

    // Contribution (0,1)
    float dx2 = dx0 - 0 - SQUISH_CONSTANT_2D;
    float dy2 = dy0 - 1 - SQUISH_CONSTANT_2D;
    float attn2 = 2 - dx2 * dx2 - dy2 * dy2;
    
    if (attn2 > 0) 
    {
      attn2 *= attn2;
      value += attn2 * attn2 * extrapolate(xsb + 0, ysb + 1, dx2, dy2);
    }
    
    if (inSum <= 1) 
    {
      float zins = 1 - inSum;
      
      if (zins > xins || zins > yins) 
      {
        if (xins > yins) 
        {
          xsv_ext = xsb + 1;
          ysv_ext = ysb - 1;
          dx_ext = dx0 - 1;
          dy_ext = dy0 + 1;
        } 
        else 
        {
          xsv_ext = xsb - 1;
          ysv_ext = ysb + 1;
          dx_ext = dx0 + 1;
          dy_ext = dy0 - 1;
        }
      } 
      else 
      {
        xsv_ext = xsb + 1;
        ysv_ext = ysb + 1;
        dx_ext = dx0 - 1 - 2 * SQUISH_CONSTANT_2D;
        dy_ext = dy0 - 1 - 2 * SQUISH_CONSTANT_2D;
      }
    } 
    else 
    {
      float zins = 2 - inSum;
      
      if (zins < xins || zins < yins) 
      {
        if (xins > yins) 
        {
          xsv_ext = xsb + 2;
          ysv_ext = ysb + 0;
          dx_ext = dx0 - 2 - 2 * SQUISH_CONSTANT_2D;
          dy_ext = dy0 + 0 - 2 * SQUISH_CONSTANT_2D;
        } 
        else 
        {
          xsv_ext = xsb + 0;
          ysv_ext = ysb + 2;
          dx_ext = dx0 + 0 - 2 * SQUISH_CONSTANT_2D;
          dy_ext = dy0 - 2 - 2 * SQUISH_CONSTANT_2D;
        }
      } 
      else 
      {
        dx_ext = dx0;
        dy_ext = dy0;
        xsv_ext = xsb;
        ysv_ext = ysb;
      }
      
      xsb += 1;
      ysb += 1;
      dx0 = dx0 - 1 - 2 * SQUISH_CONSTANT_2D;
      dy0 = dy0 - 1 - 2 * SQUISH_CONSTANT_2D;
    }
    
    // Contribution (0,0) or (1,1)
    float attn0 = 2 - dx0 * dx0 - dy0 * dy0;
    
    if (attn0 > 0) 
    {
      attn0 *= attn0;
      value += attn0 * attn0 * extrapolate(xsb, ysb, dx0, dy0);
    }
    
    // Extra Vertex
    float attn_ext = 2 - dx_ext * dx_ext - dy_ext * dy_ext;
    
    if (attn_ext > 0) 
    {
      attn_ext *= attn_ext;
      value += attn_ext * attn_ext * extrapolate(xsv_ext, ysv_ext, dx_ext, dy_ext);
    }
    
    return value;
  }
  
  private float extrapolate(int xsb, int ysb, float dx, float dy)
  {
    int index = perm[xsb & PMASK] ^ (ysb & PMASK);
    
    Grad2 grad = grad2[perm[index] % grad2.length];

    return (grad.dx * dx + grad.dy * dy) / N2;
  }
  
      private static final float N2 = 7.69084574549313f;
  
  private static int fastFloor(float x) 
  {
    int xi = (int)x;
    return x < xi ? xi - 1 : xi;
  }
  
  public static class Grad2 
  {

    
    float dx, dy;
    
    public Grad2(float dx, float dy) 
    {
      this.dx = dx ; this.dy = dy;
    }
  }
  
    private static final Grad2[] grad2 = 
    {
      new Grad2( 0.130526192220052f,  0.99144486137381f),
      new Grad2( 0.38268343236509f,   0.923879532511287f),
      new Grad2( 0.608761429008721f,  0.793353340291235f),
      new Grad2( 0.793353340291235f,  0.608761429008721f),
      new Grad2( 0.923879532511287f,  0.38268343236509f),
      new Grad2( 0.99144486137381f,   0.130526192220051f),
      new Grad2( 0.99144486137381f,  -0.130526192220051f),
      new Grad2( 0.923879532511287f, -0.38268343236509f),
      new Grad2( 0.793353340291235f, -0.60876142900872f),
      new Grad2( 0.608761429008721f, -0.793353340291235f),
      new Grad2( 0.38268343236509f,  -0.923879532511287f),
      new Grad2( 0.130526192220052f, -0.99144486137381f),
      new Grad2(-0.130526192220052f, -0.99144486137381f),
      new Grad2(-0.38268343236509f,  -0.923879532511287f),
      new Grad2(-0.608761429008721f, -0.793353340291235f),
      new Grad2(-0.793353340291235f, -0.608761429008721f),
      new Grad2(-0.923879532511287f, -0.38268343236509f),
      new Grad2(-0.99144486137381f,  -0.130526192220052f),
      new Grad2(-0.99144486137381f,   0.130526192220051f),
      new Grad2(-0.923879532511287f,  0.38268343236509f),
      new Grad2(-0.793353340291235f,  0.608761429008721f),
      new Grad2(-0.608761429008721f,  0.793353340291235f),
      new Grad2(-0.38268343236509f,   0.923879532511287f),
      new Grad2(-0.130526192220052f,  0.99144486137381f)
    };
}
