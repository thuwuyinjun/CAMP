package it.uniroma3.mat.extendedset.intset;

import it.uniroma3.mat.extendedset.utilities.BitCount;

import java.util.NoSuchElementException;

/**
 */
public class ConciseSetUtils
{
  /**
   * The highest representable integer.
   * <p/>
   * Its value is computed as follows. The number of bits required to
   * represent the longest sequence of 0's or 1's is
   * <tt>ceil(log<sub>2</sub>(({@link Integer#MAX_VALUE} - 31) / 31)) = 27</tt>.
   * Indeed, at least one literal exists, and the other bits may all be 0's or
   * 1's, that is <tt>{@link Integer#MAX_VALUE} - 31</tt>. If we use:
   * <ul>
   * <li> 2 bits for the sequence type;
   * <li> 5 bits to indicate which bit is set;
   * </ul>
   * then <tt>32 - 5 - 2 = 25</tt> is the number of available bits to
   * represent the maximum sequence of 0's and 1's. Thus, the maximal bit that
   * can be set is represented by a number of 0's equals to
   * <tt>31 * (1 << 25)</tt>, followed by a literal with 30 0's and the
   * MSB (31<sup>st</sup> bit) equal to 1
   */
  public final static int MAX_ALLOWED_INTEGER = 31 * (1 << 25) + 30; // 1040187422

  /**
   * The lowest representable integer.
   */
  public final static int MIN_ALLOWED_SET_BIT = 0;

  /**
   * Maximum number of representable bits within a literal
   */
  public final static int MAX_LITERAL_LENGTH = 31;

  /**
   * Literal that represents all bits set to 1 (and MSB = 1)
   */
  public final static int ALL_ONES_LITERAL = 0xFFFFFFFF;

  /**
   * Literal that represents all bits set to 0 (and MSB = 1)
   */
  public final static int ALL_ZEROS_LITERAL = 0x80000000;

  /**
   * All bits set to 1 and MSB = 0
   */
  public final static int ALL_ONES_WITHOUT_MSB = 0x7FFFFFFF;

  /**
   * Sequence bit
   */
  public final static int SEQUENCE_BIT = 0x40000000;
  
  
//改开始,add some constant value
  /**
   * 	Maximum number of 0-fills or 1-fills can be compressed
   */
  public final static int MAX_FILL_NUM = 0x0fffffff;
  
  /**
   * 	Maximum number of fills compressed in the F-L-F 
   */
  public final static int MAX_L_F_L_NUM =0x0000007f;
  /**
   * Maximum number of the fills compressed in the F-L-F
   */
  public final static int MAX_F_L_F_NUM = 0x000000ff;
  /**
   * The header of the sequence 0-FILL
  */
  public final static int SEQUENCE_0_FILL = 0;
  /**
   * The header of the sequence 1-FILL
   */
  public final static int SEQUENCE_1_FILL = 0x10000000;
  /*
   * The header of the sequence 0L-F-1L
   */
  public final static int SEQUENCE_0L_F_1L = 0x40000000;
  /*
   * The header of the sequence 1L-F-0L
   */
  public final static int SEQUENCE_1L_F_0L = 0x50000000;
  /*
   * The header of the sequence 0L1-F-0L2
   */
  public final static int SEQUENCE_0L1_F_0L2 = 0x20000000;
  /*
   * The header of the sequence 1L1-F-1L2
   */
  public final static int SEQUENCE_1L1_F_1L2 = 0x30000000;
  /*
   * The header of the sequence F1-L-F2
   */
  public final static int SEQUENCE_F1_L_F2 = 0x60000000;
  
  /*
   * Prepare for judging whether a literal can be consider to have a DirtyByte
   */
  public final static int[] SECOMPAX_0_MASK = {0x000000ff,0x0000ff00,0x00ff0000,0x7f000000};
  
  //改结束
  /**
   * Calculates the modulus division by 31 in a faster way than using <code>n % 31</code>
   * <p/>
   * This method of finding modulus division by an integer that is one less
   * than a power of 2 takes at most <tt>O(lg(32))</tt> time. The number of operations
   * is at most <tt>12 + 9 * ceil(lg(32))</tt>.
   * <p/>
   * See <a
   * href="http://graphics.stanford.edu/~seander/bithacks.html">http://graphics.stanford.edu/~seander/bithacks.html</a>
   *
   * @param n number to divide
   *
   * @return <code>n % 31</code>
   */
  public static int maxLiteralLengthModulus(int n)
  {
    int m = (n & 0xC1F07C1F) + ((n >>> 5) & 0xC1F07C1F);
    m = (m >>> 15) + (m & 0x00007FFF);
    if (m <= 31) {
      return m == 31 ? 0 : m;
    }
    m = (m >>> 5) + (m & 0x0000001F);
    if (m <= 31) {
      return m == 31 ? 0 : m;
    }
    m = (m >>> 5) + (m & 0x0000001F);
    if (m <= 31) {
      return m == 31 ? 0 : m;
    }
    m = (m >>> 5) + (m & 0x0000001F);
    if (m <= 31) {
      return m == 31 ? 0 : m;
    }
    m = (m >>> 5) + (m & 0x0000001F);
    if (m <= 31) {
      return m == 31 ? 0 : m;
    }
    m = (m >>> 5) + (m & 0x0000001F);
    return m == 31 ? 0 : m;
  }

  /**
   * Calculates the multiplication by 31 in a faster way than using <code>n * 31</code>
   *
   * @param n number to multiply
   *
   * @return <code>n * 31</code>
   */
  public static int maxLiteralLengthMultiplication(int n)
  {
    return (n << 5) - n;
  }

  /**
   * Calculates the division by 31
   *
   * @param n number to divide
   *
   * @return <code>n / 31</code>
   */
  public static int maxLiteralLengthDivision(int n)
  {
    return n / 31;
  }

  /**
   * Checks whether a word is a literal one
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a literal word
   */
  public static boolean isLiteral(int word)
  {
    // "word" must be 1*
    // NOTE: this is faster than "return (word & 0x80000000) == 0x80000000"
    return (word & 0x80000000) != 0;
  }

  /**
   * Checks whether a word contains a sequence of 1's
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 1's
   */
  public static boolean isOneSequence(int word)
  {
    // "word" must be 01*
    return (word & 0xc0000000) == SEQUENCE_BIT;
  }

  /**
   * Checks whether a word contains a sequence of 0's
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's
   */
  public static boolean isZeroSequence(int word)
  {
    // "word" must be 00*
    return (word & 0xc0000000) == 0;
  }

  /**
   * Checks whether a word contains a sequence of 0's with no set bit, or 1's
   * with no unset bit.
   * <p/>
   * <b>NOTE:</b> when {@link #simulateWAH} is <code>true</code>, it is
   * equivalent to (and as fast as) <code>!</code>{@link #isLiteral(int)}
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's or 1's
   *         but with no (un)set bit
   */
  public static boolean isSequenceWithNoBits(int word)
  {
    // "word" must be 0?00000*
    return (word & 0xBE000000) == 0x00000000;
  }

  /**
   * Gets the number of blocks of 1's or 0's stored in a sequence word
   *
   * @param word word to check
   *
   * @return the number of blocks that follow the first block of 31 bits
   */
  public static int getSequenceCount(int word)
  {
    // get the 25 LSB bits
    return word & 0x0FFFFFFF;		//改
  }

  public static int getSequenceNumWords(int word)
  {
    return getSequenceCount(word) + 1;
  }

  /**
   * Clears the (un)set bit in a sequence
   *
   * @param word word to check
   *
   * @return the sequence corresponding to the given sequence and with no
   *         (un)set bits
   */
  public static int getSequenceWithNoBits(int word)
  {
    // clear 29 to 25 LSB bits
    return (word & 0xC1FFFFFF);
  }

  /**
   * Gets the literal word that represents the first 31 bits of the given the
   * word (i.e. the first block of a sequence word, or the bits of a literal word).
   * <p/>
   * If the word is a literal, it returns the unmodified word. In case of a
   * sequence, it returns a literal that represents the first 31 bits of the
   * given sequence word.
   *
   * @param word word to check
   *
   * @return the literal contained within the given word, <i>with the most
   *         significant bit set to 1</i>.
   */
  public static int getLiteral(int word, boolean simulateWAH)
  {
    if (isLiteral(word)) {
      return word;
    }

    if (simulateWAH) {
      return isZeroSequence(word) ? ALL_ZEROS_LITERAL : ALL_ONES_LITERAL;
    }

    // get bits from 30 to 26 and use them to set the corresponding bit
    // NOTE: "1 << (word >>> 25)" and "1 << ((word >>> 25) & 0x0000001F)" are equivalent
    // NOTE: ">>> 1" is required since 00000 represents no bits and 00001 the LSB bit set
    int literal = (1 << (word >>> 25)) >>> 1;
    return isZeroSequence(word)
           ? (ALL_ZEROS_LITERAL | literal)
           : (ALL_ONES_LITERAL & ~literal);
  }

  public static int getLiteralFromZeroSeqFlipBit(int word)
  {
    int flipBit = getFlippedBit(word);
    if (flipBit > -1) {
      return ALL_ZEROS_LITERAL | flipBitAsBinaryString(flipBit);
    }
    return ALL_ZEROS_LITERAL;
  }

  public static int getLiteralFromOneSeqFlipBit(int word)
  {
    int flipBit = getFlippedBit(word);
    if (flipBit > -1) {
      return ALL_ONES_LITERAL ^ flipBitAsBinaryString(flipBit);
    }
    return ALL_ONES_LITERAL;
  }

  /**
   * Gets the position of the flipped bit within a sequence word. If the
   * sequence has no set/unset bit, returns -1.
   * <p/>
   * Note that the parameter <i>must</i> a sequence word, otherwise the
   * result is meaningless.
   *
   * @param word sequence word to check
   *
   * @return the position of the set bit, from 0 to 31. If the sequence has no
   *         set/unset bit, returns -1.
   */
  public static int getFlippedBit(int word)
  {
    // get bits from 30 to 26
    // NOTE: "-1" is required since 00000 represents no bits and 00001 the LSB bit set
    return ((word >>> 25) & 0x0000001F) - 1;
  }

  public static int flipBitAsBinaryString(int flipBit)
  {
    return ((Number) Math.pow(2, flipBit)).intValue();
  }

  /**
   * Gets the number of set bits within the literal word
   *
   * @param word literal word
   *
   * @return the number of set bits within the literal word
   */
  public static int getLiteralBitCount(int word)
  {
    return BitCount.count(getLiteralBits(word));
  }

  /**
   * Gets the bits contained within the literal word
   *
   * @param word literal word
   *
   * @return the literal word with the most significant bit cleared
   */
  public static int getLiteralBits(int word)
  {
    return ALL_ONES_WITHOUT_MSB & word;
  }

  public static boolean isAllOnesLiteral(int word)
  {
    return (word & -1) == -1;
  }

  public static boolean isAllZerosLiteral(int word)
  {
    return (word | 0x80000000) == 0x80000000;
  }

  public static boolean isLiteralWithSingleZeroBit(int word)
  {
    return isLiteral(word) && (Integer.bitCount(~word) == 1);
  }

  public static boolean isLiteralWithSingleOneBit(int word)
  {
    return isLiteral(word) && (Integer.bitCount(word) == 2);
  }

  public static int clearBitsAfterInLastWord(int lastWord, int lastSetBit)
  {
    return lastWord &= ALL_ZEROS_LITERAL | (0xFFFFFFFF >>> (31 - lastSetBit));
  }
  
  
//改开始
  
  /**
   * 	return whether a word is a 0-fill
   */
  
  public static boolean is0_fill(int word)
  {
	  return (word & 0xf0000000) == 0;
  }
  /**
   * return whether a word is a 1-fill
   */
  
  public static boolean is1_fill(int word)
  {
	  return (word & 0xf0000000) == 0x10000000;
  }
  /**
   * return the number of the DirtyByte0 in a word
   */
  public static boolean isDirtyByte0Word(int word)
  {
	  int num = 0;
	  if(!(isLiteral(word)))
		  return false;
	  for(int i = 0;i<4 ;i++)
	  {
		  if((word & SECOMPAX_0_MASK[i]) != 0)
		  {
			num++;  
		  }
	  }
	  return num == 1;
  }
  
  /*
   * return the position of the DirtyByte0 in the DirtyByteWord
   */
  public static int getDirtyByte0Pos(int word)
  {
	  int i;
	  if(isDirtyByte0Word(word))
	  {
		  for(i = 0;i<4 ;i++)
		  {
			  if((word & SECOMPAX_0_MASK[i]) != 0)
			  {
				  break;  
			  }
		  }
		  return i;
	  }
	  return -1;
  }
  
  
  /*
   * return the number of the DirtyByte1 in a word
   */
  public static boolean isDirtyByte1Word(int word)
  {
	  int num = 0;
	  if(!(isLiteral(word)))
	  {
		  return false;
	  }
	  for(int i = 0;i<4 ;i++)
	  {
		  if((~word & SECOMPAX_0_MASK[i]) != 0)
		  {
			num++;  
		  }
	  }
	  return num == 1;
  }
  
  /*
   * return the position of the DirtyByte0 in the DirtyByteWord
   */
  public static int getDirtyByte1Pos(int word)
  {
	  int i;
	  if(isDirtyByte1Word(word))
	  {
		  for(i = 0;i<4 ;i++)
		  {
			  if((~word & SECOMPAX_0_MASK[i]) != 0)
			  {
				  break;  
			  }
		  }
		  return i;
	  }
	  return -1;
  }
  /**
   * Checks whether a word is 0L-F-1L
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's
   */
  public static boolean is0L_F_1L(int word)
  {
    // "word" must be 00*
    return (word & 0xf0000000) == SEQUENCE_0L_F_1L;
  }
  /**
   * Checks whether a word is 1L-F-0L
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's
   */
  public static boolean is1L_F_0L(int word)
  {
    // "word" must be 00*
    return (word & 0xf0000000) == SEQUENCE_1L_F_0L;
  }
  /**
   * Checks whether a word is 0L1-F-0L2
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's
   */
  public static boolean is0L1_F_0L2(int word)
  {
    // "word" must be 00*
    return (word & 0xf0000000) == SEQUENCE_0L1_F_0L2;
  }
  /**
   * Checks whether a word is 1L1-F-1L2
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's
   */
  public static boolean is1L1_F_1L2(int word)
  {
    // "word" must be 00*
    return (word & 0xf0000000) == SEQUENCE_1L1_F_1L2;
  }
  /**
   * Checks whether a word is F1-L-F2
   *
   * @param word word to check
   *
   * @return <code>true</code> if the given word is a sequence of 0's
   */
  public static boolean isF1_L_F2(int word)
  {
    // "word" must be 00*
    return (word & 0xe0000000) == SEQUENCE_F1_L_F2;
  }
  
  public static int getDirty0(int word)
  {
	  int pos = 0;
	  if(isDirtyByte0Word(word))
	  {
		  pos = getDirtyByte0Pos(word);
		  //return (word & (0x000000ff << (8*pos))) >>> (8*pos);
		  switch(pos){
		  case 0: return word & 0x000000ff;
		  case 1: return (word & 0x0000ff00) >>> 8;
		  case 2: return (word & 0x00ff0000) >>> 16;
		  default: return (word & 0x7f000000) >>> 24;
		  }
	  }
	  return 0;
  }
  /*
   * return the Dirtybyte in the nearly-1-fill
   */
  public static int getDirty1(int word)
  {
	  int pos = 0;
	  if(isDirtyByte1Word(word))
	  {
		  pos = getDirtyByte1Pos(word);
		  //return (word & (0x000000ff << (8*pos))) >>> (8*pos);
		  switch(pos){
		  case 0: return (~word & 0x000000ff) ^ 0x000000ff;
		  case 1: return ((~word & 0x0000ff00) >>> 8) ^ 0x00000ff;
		  case 2: return ((~word & 0x00ff0000) >>> 16) ^ 0x000000ff;
		  default: return ((~word & 0x7f000000) >>> 24) ^ 0x000000ff;
		  }
	  }
	  return 0;
  }
  /*
   *  When meeting the F1-L-F2,the number of the Fill words is useful
   *  This method is used for getting the number of the Fill words in the 2 Fill compressed in the F1-L-F2 word.
   */
  public static int[] getFLFFILLWords(int word)
  {
	  int[] num = new int [2];
	  if(isF1_L_F2(word))
	  {
		  num[0] = (word & 0x00ff0000) >>> 16;
		  num[1] = word & 0x000000ff;
		  return num;
	  }
	  return null;
  }
  
  /*
   *  When meeting the L-F-L,the number of the Fill words is useful
   *  This method is used for getting the number of the Fill words in the Fill compressed in the L-F-L word.
   */
  public static int getLFLFILLWords(int word)
  {
	  int num;
	  if(is0L_F_1L(word) || is1L_F_0L(word) || is0L1_F_0L2(word) || is1L1_F_1L2(word))
	  {
		  num = (word & 0x007f0000) >>> 16;
		  return num;
	  }
	  return 0;
  }
  
  /*
   *  Return the Dirtybytes of the L-F-L.
   */
  public static int[] getLFLLiteralWords(int word)
  {
	  int[] num = new int [2];
	  if(is0L_F_1L(word) || is1L_F_0L(word) || is0L1_F_0L2(word) || is1L1_F_1L2(word))
	  {
		  num[0] = (word & 0x0000ff00) >>> 8;
		  num[1] = word & 0x000000ff;
		  return num;
	  }
	  return null;
  }
  
  /*
   *  Return the Dirtybyte of the F-L-F.
   */
  public static int getFLFLiteralWords(int word)
  {
	  int num;
	  if(isF1_L_F2(word))
	  {
		  num = (word & 0x0000ff00) >> 8;
		  return num;
	  }
	  return 0;
  }
  
  
  //改结束
  public interface WordExpander
  {
    public boolean hasNext();

    public boolean hasPrevious();

    public int next();

    public int previous();

    public void skipAllAfter(int i);

    public void skipAllBefore(int i);

    public void reset(int offset, int word, boolean fromBeginning);

    public WordExpander clone();
  }

  public static LiteralAndZeroFillExpander newLiteralAndZeroFillExpander()
  {
    return new LiteralAndZeroFillExpander();
  }

  /**
   * Iterator over the bits of literal and zero-fill words
   */
  public static class LiteralAndZeroFillExpander implements WordExpander
  {
    final int[] buffer = new int[MAX_LITERAL_LENGTH];
    int len = 0;
    int current = 0;

    @Override
    public boolean hasNext()
    {
      return current < len;
    }

    @Override
    public boolean hasPrevious()
    {
      return current > 0;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return buffer[current++];
    }

    @Override
    public int previous()
    {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      return buffer[--current];
    }

    @Override
    public void skipAllAfter(int i)
    {
      while (hasPrevious() && buffer[current - 1] > i) {
        current--;
      }
    }

    @Override
    public void skipAllBefore(int i)
    {
      while (hasNext() && buffer[current] < i) {
        current++;
      }
    }

    @Override
    public void reset(int offset, int word, boolean fromBeginning)
    {
      if (isLiteral(word)) {
        len = 0;
        for (int i = 0; i < MAX_LITERAL_LENGTH; i++) {
          if ((word & (1 << i)) != 0) {
            buffer[len++] = offset + i;
          }
        }
        current = fromBeginning ? 0 : len;
      } else {
        if (is0_fill(word)) {
          //if (isSequenceWithNoBits(word)) {
            len = 0;
            current = 0;
          /*} else {
            len = 1;
            buffer[0] = offset + ((0x3FFFFFFF & word) >>> 25) - 1;
            current = fromBeginning ? 0 : 1;*/
          }
         else {
          throw new RuntimeException("sequence of ones!");
        }
      }
    }

    @Override
    public WordExpander clone()
    {
      LiteralAndZeroFillExpander retVal = new LiteralAndZeroFillExpander();
      System.arraycopy(buffer, 0, retVal.buffer, 0, buffer.length);
      retVal.len = len;
      retVal.current = current;
      return retVal;
    }
  }

  public static OneFillExpander newOneFillExpander()
  {
    return new OneFillExpander();
  }
  public static class OneFillExpander implements WordExpander
  {
    int firstInt = 1;
    int lastInt = -1;
    int current = 0;
    int exception = -1;

    @Override
    public boolean hasNext()
    {
      return current < lastInt;
    }

    @Override
    public boolean hasPrevious()
    {
      return current > firstInt;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      current++;
      /*if (current == exception) {
        current++;
      }*/
      return current;
    }

    @Override
    public int previous()
    {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      current--;
      if (current == exception) {
        current--;
      }
      return current;
    }

    @Override
    public void skipAllAfter(int i)
    {
      if (i >= current) {
        return;
      }
      current = i + 1;
    }

    @Override
    public void skipAllBefore(int i)
    {
      if (i <= current) {
        return;
      }
      current = i - 1;
    }

    @Override
    public void reset(int offset, int word, boolean fromBeginning)
    {
      if (!is1_fill(word)) {
        throw new RuntimeException("NOT a sequence of ones!");
      }
      firstInt = offset;
      lastInt = offset + maxLiteralLengthMultiplication(getSequenceCount(word)) - 1;

      /*exception = offset + ((0x3FFFFFFF & word) >>> 25) - 1;
      if (exception == firstInt) {
        firstInt++;
      }
      if (exception == lastInt) {
        lastInt--;
      }*/

      current = fromBeginning ? (firstInt - 1) : (lastInt + 1);
    }

    @Override
    public WordExpander clone()
    {
      OneFillExpander retVal = new OneFillExpander();
      retVal.firstInt = firstInt;
      retVal.lastInt = lastInt;
      retVal.current = current;
      retVal.exception = exception;
      return retVal;
    }
  }

  /**
   * Iterator over the bits of one-fill words
   */
  /*public static class OneFillExpander implements WordExpander
  {
    int firstInt = 1;
    int lastInt = -1;
    int current = 0;
    int exception = -1;

    @Override
    public boolean hasNext()
    {
      return current < lastInt;
    }

    @Override
    public boolean hasPrevious()
    {
      return current > firstInt;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      current++;
      if (current == exception) {
        current++;
      }
      return current;
    }

    @Override
    public int previous()
    {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      current--;
      if (current == exception) {
        current--;
      }
      return current;
    }

    @Override
    public void skipAllAfter(int i)
    {
      if (i >= current) {
        return;
      }
      current = i + 1;
    }

    @Override
    public void skipAllBefore(int i)
    {
      if (i <= current) {
        return;
      }
      current = i - 1;
    }

    @Override
    public void reset(int offset, int word, boolean fromBeginning)
    {
      if (!is1_fill(word)) {
        throw new RuntimeException("NOT a sequence of ones!");
      }
      firstInt = offset;
      lastInt = offset + maxLiteralLengthMultiplication(getSequenceCount(word)) - 1;

      exception = offset + ((0x3FFFFFFF & word) >>> 25) - 1;
      if (exception == firstInt) {
        firstInt++;
      }
      if (exception == lastInt) {
        lastInt--;
      }

      current = fromBeginning ? (firstInt - 1) : (lastInt + 1);
    }

    @Override
    public WordExpander clone()
    {
      OneFillExpander retVal = new OneFillExpander();
      retVal.firstInt = firstInt;
      retVal.lastInt = lastInt;
      retVal.current = current;
      retVal.exception = exception;
      return retVal;
    }
  }
  */
  public static FLFExpander newFLFExpander()
  {
    return new FLFExpander();
  }

  /**
   * Iterator over the bits of one-fill words
   */
  public static class FLFExpander implements WordExpander

  {
    int firstInt = 1;
    int lastInt = -1;
    int current = 0;
    int [] buffer;
    int len = 0;

    @Override
    public boolean hasNext()
    {
      return current < len;
    }

    @Override
    public boolean hasPrevious()
    {
      return current > 0;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return buffer[current++];
    }

    @Override
    public int previous()
    {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      return buffer[--current];
    }

    @Override
    public void skipAllAfter(int i)
    {
      if (i >= current) {
        return;
      }
      current = i + 1;
    }

    @Override
    public void skipAllBefore(int i)
    {
      if (i <= current) {
        return;
      }
      current = i - 1;
    }

    @Override
    public void reset(int offset, int word, boolean fromBeginning)
    {
      int i= 0;
      int[] fillnum = new int[2];
      int[] fillType = new int [2];
      int literal;
      int literalType;
      int literalPos;
      int literalcount;
      int literalByte = getFLFLiteralWords(word);
      fillnum = getFLFFILLWords(word);
      fillType[0] = (word & 0x10000000) >>> 28;
      fillType[1] = (word & 0x04000000) >>> 26;
      literalType = (word & 0x08000000) >>> 27;
      literalPos = (word & 0x03000000) >>> 24;
      
      len += maxLiteralLengthMultiplication(fillnum[0] * fillType[0]);
      if(literalType == 0)
      {
    	  literal = (literalByte << (literalPos * 8)) | 0x80000000;
      }
      else
      {
    	  literal = (literalByte <<(literalPos * 8));
    	  switch(literalPos)
    	  {
    	  case 0: literal |= 0xffffff00; break;
    	  case 1: literal |= 0xffff00ff; break;
    	  case 2: literal |= 0xff00ffff; break;
    	  default : literal |= 0x80ffffff;
    	  }
      }
      literalcount = getLiteralBitCount(literal);
     
      len += literalcount;
      
      len += maxLiteralLengthMultiplication(fillnum[1] * fillType[1]);
      
      buffer = new int[len];
      for(i =0 ;i<maxLiteralLengthMultiplication(fillnum[0] * fillType[0]);i++)
      {
    	  buffer[i] = offset + i;
      }
      
      offset += maxLiteralLengthMultiplication(fillnum[0]);
      for(int j = 0 ;j<MAX_LITERAL_LENGTH ;j++)
      {
    	  if ((literal & (1 << j)) != 0) 
              buffer[i++] = offset + j;
      }
      
      offset += MAX_LITERAL_LENGTH;
      
      for(int j=0 ; j< maxLiteralLengthMultiplication(fillnum[1] * fillType[1]); i++,j++)
      {
    	  buffer[i] = offset + j;
      }
      
      lastInt = buffer[len - 1] + 1;
      //current = fromBeginning ? (firstInt - 1) : (lastInt + 1);
    }

    @Override
    public WordExpander clone()
    {
      FLFExpander retVal = new FLFExpander();
      retVal.firstInt = firstInt;
      retVal.lastInt = lastInt;
      retVal.current = current;
      retVal.buffer = buffer;
      return retVal;
    }
  }
  
  public static LFLExpander newLFLExpander()
  {
    return new LFLExpander();
  }
  public static class LFLExpander implements WordExpander

  {
    int firstInt = 1;
    int lastInt = -1;
    int current = 0;
    int [] buffer;
    int len = 0;

    @Override
    public boolean hasNext()
    {
      return current < len;
    }

    @Override
    public boolean hasPrevious()
    {
      return current > 0;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return buffer[current++];
    }

    @Override
    public int previous()
    {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      return buffer[--current];
    }

    @Override
    public void skipAllAfter(int i)
    {
      if (i >= current) {
        return;
      }
      current = i + 1;
    }

    @Override
    public void skipAllBefore(int i)
    {
      if (i <= current) {
        return;
      }
      current = i - 1;
    }

    @Override
    public void reset(int offset, int word, boolean fromBeginning)
    {
      int i= 0;
      int fillnum ;
      int fillType ;
      int[] literal = new int[2];
      int[] literalPos = new int [2];
      int[] literalcount = new int[2];
      int[] literalByte = getLFLLiteralWords(word);
      fillnum = getLFLFILLWords(word);
      fillType = (word & 0x00800000) >>> 23;
      literalPos[0] = (word & 0x0c000000) >>> 26;
      literalPos[1] = (word & 0x03000000) >>> 24;
      if(is0L1_F_0L2(word))
      {
    	  literal[0] =  0x80000000 | (literalByte[0] <<(literalPos[0] * 8));
    	  literal[1] =  0x80000000 | (literalByte[1] <<(literalPos[1] * 8));
      }
      else
      {
    	  if(is0L_F_1L(word))
    	  {
    		  literal[0] = 0x80000000 | (literalByte[0] <<(literalPos[0] * 8));
    		  literal[1] = (literalByte[1] <<(literalPos[1] * 8));
        	  switch(literalPos[1])
        	  {
        	  case 0: literal[1] |= 0xffffff00; break;
        	  case 1: literal[1] |= 0xffff00ff; break;
        	  case 2: literal[1] |= 0xff00ffff; break;
        	  default : literal[1] |= 0x80ffffff;
        	  }
    	  }
    	  else
    	  {
    		  if(is1L_F_0L(word))
    		  {
    			  literal[0] = (literalByte[0] <<(literalPos[0] * 8));
    	    	  switch(literalPos[0])
    	    	  {
    	    	  case 0: literal[0] |= 0xffffff00; break;
    	    	  case 1: literal[0] |= 0xffff00ff; break;
    	    	  case 2: literal[0] |= 0xff00ffff; break;
    	    	  default : literal[0] |= 0x80ffffff;
    	    	  }
    			  literal[1] = 0x80000000 | (literalByte[1] <<(literalPos[1] * 8));
    		  }
    		  else
    		  {
    			  literal[0] = (literalByte[0] <<(literalPos[0] * 8));
    	    	  switch(literalPos[0])
    	    	  {
    	    	  case 0: literal[0] |= 0xffffff00; break;
    	    	  case 1: literal[0] |= 0xffff00ff; break;
    	    	  case 2: literal[0] |= 0xff00ffff; break;
    	    	  default : literal[0] |= 0x80ffffff;
    	    	  }
    	    	  literal[1] = (literalByte[1] <<(literalPos[1] * 8));
    	    	  switch(literalPos[1])
    	    	  {
    	    	  case 0: literal[1] |= 0xffffff00; break;
    	    	  case 1: literal[1] |= 0xffff00ff; break;
    	    	  case 2: literal[1] |= 0xff00ffff; break;
    	    	  default : literal[1] |= 0x80ffffff;
    	    	  }
    		  }
    	  }
      }
      
      len += maxLiteralLengthMultiplication(fillnum * fillType);
      
      literalcount[0] = getLiteralBitCount(literal[0]);
      literalcount[1] = getLiteralBitCount(literal[1]);
      len += literalcount[0] + literalcount[1];
      
      buffer = new int[len];
      for(int j = 0 ;j<MAX_LITERAL_LENGTH ;j++)
      {
    	  if ((literal[0] & (1 << j)) != 0) 
              buffer[i++] = offset + j;
      }
      
      offset += MAX_LITERAL_LENGTH;
      for(int j =0 ;j<maxLiteralLengthMultiplication(fillnum * fillType);j++,i++)
      {
    	  buffer[i] = offset + j;
      }
      
      offset += fillnum * 31;
      for(int j = 0 ;j<MAX_LITERAL_LENGTH ;j++)
      {
    	  if ((literal[1] & (1 << j)) != 0) 
              buffer[i++] = offset + j;
      }
      
      offset += MAX_LITERAL_LENGTH;
      
      lastInt = buffer[len - 1] + 1;
      //current = fromBeginning ? (firstInt - 1) : (lastInt + 1);
    }

    @Override
    public WordExpander clone()
    {
      FLFExpander retVal = new FLFExpander();
      retVal.firstInt = firstInt;
      retVal.lastInt = lastInt;
      retVal.current = current;
      retVal.buffer = buffer;
      return retVal;
    }
  }
}
