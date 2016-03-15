package org.CAMP;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class COMPAXSet
{
  private final static int CHUNK_SIZE = 10000;
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
  
  
//閿熶茎鍖℃嫹濮�,add some constant value
  /**
   * 	Maximum number of 0-fills or 1-fills can be compressed
   */
  public final static int MAX_FILL_NUM = 0x1fffffff;
  
  /**
   * 	Maximum number of fills compressed in the F-L-F 
   */
  public final static int MAX_L_F_L_NUM =0x000000ff;
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
  public final static int SEQUENCE_1_FILL = 0x60000000;
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
  //public final static int SEQUENCE_0L1_F_0L2 = 0x20000000;
  /*
   * The header of the sequence 1L1-F-1L2
   */
  public final static int SEQUENCE_1L1_F_1L2 = 0x30000000;
  /*
   * The header of the sequence F1-L-F2
   */
  public final static int SEQUENCE_F1_L_F2 = 0x40000000;
  
  /*
   * Prepare for judging whether a literal can be consider to have a DirtyByte
   */
  public final static int[] SECOMPAX_0_MASK = {0x000000ff,0x0000ff00,0x00ff0000,0x7f000000};
  
  //閿熶茎鏂ゆ嫹閿熸枻鎷�
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
    //return word & 0x0FFFFFFF;		
    return word & 0x1FFFFFFF;
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
  
  
//閿熶茎鍖℃嫹濮�
  
  /**
   * 	return whether a word is a 0-fill
   */
  
  public static boolean is0_fill(int word)
  {
	  //return (word & 0xf0000000) == 0;
	  return (word & 0xf0000000) == 0;
  }
  /**
   * return whether a word is a 1-fill
   */
  
  public static boolean is1_fill(int word)
  {
	  //return (word & 0xf0000000) == SEQUENCE_1_FILL;
	  return (word & 0xe0000000) == SEQUENCE_1_FILL;
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
    return (word & 0xe0000000) == SEQUENCE_0L1_F_0L2;
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
    //return (word & 0xe0000000) == SEQUENCE_F1_L_F2;
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
		  num = (word & 0x00ff0000) >>> 16;
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
  
  
  //閿熶茎鏂ゆ嫹閿熸枻鎷�
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
      int literal;
      int literalPos;
      int literalcount;
      int literalByte = getFLFLiteralWords(word);
      fillnum = getFLFFILLWords(word);
      //fillType[1] = (word & 0x04000000) >>> 26;
      //literalType = (word & 0x08000000) >>> 27;
      //literalPos = (word & 0x03000000) >>> 24;
      literalPos = (word & 0x06000000) >>> 25;
      
      len += maxLiteralLengthMultiplication(fillnum[0]);
      //if(literalType == 0)
      //{
    	  literal = (literalByte << (literalPos * 8)) | 0x80000000;
      //}
      /*else
      {
    	  literal = (literalByte <<(literalPos * 8));
    	  switch(literalPos)
    	  {
    	  case 0: literal |= 0xffffff00; break;
    	  case 1: literal |= 0xffff00ff; break;
    	  case 2: literal |= 0xff00ffff; break;
    	  default : literal |= 0x80ffffff;
    	  }
      }*/
      literalcount = getLiteralBitCount(literal);
     
      len += literalcount;
      
      len += maxLiteralLengthMultiplication(fillnum[1]);
      
      buffer = new int[len];
      for(i =0 ;i<maxLiteralLengthMultiplication(fillnum[0]);i++)
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
      
      for(int j=0 ; j< maxLiteralLengthMultiplication(fillnum[1]); i++,j++)
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
      int[] literal = new int[2];
      int[] literalPos = new int [2];
      int[] literalcount = new int[2];
      int[] literalByte = getLFLLiteralWords(word);
      fillnum = getLFLFILLWords(word);
     // fillType = (word & 0x00800000) >>> 23;
      literalPos[0] = (word & 0x18000000) >>> 27;
      literalPos[1] = (word & 0x06000000) >>> 25;
      if(is0L1_F_0L2(word))
      {
    	  literal[0] =  0x80000000 | (literalByte[0] <<(literalPos[0] * 8));
    	  literal[1] =  0x80000000 | (literalByte[1] <<(literalPos[1] * 8));
      }
      /*else
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
      }*/
      
      len += maxLiteralLengthMultiplication(fillnum);
      
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
      for(int j =0 ;j<maxLiteralLengthMultiplication(fillnum);j++,i++)
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

  public static COMPAXSet bitmapof(int []data)
  {
	  ConciseSet concise = new ConciseSet(true);
	  
	  for(int i = 0; i<data.length; i++)
	  {
		  concise.add(data[i]);
	  }
	  
	  return COMPAXSet.compact(COMPAXSet.newImmutableFromMutable(concise));
  }
  
  public static COMPAXSet newImmutableFromMutable(ConciseSet conciseSet)
  {
    if (conciseSet == null || conciseSet.isEmpty()) {
      return new COMPAXSet();
    }
    int[] words = conciseSet.getWords();
    //IntList retVal = new IntList();
    return new COMPAXSet(IntBuffer.wrap(words));
    //IntBuffer buffer =  IntBuffer.wrap(retVal.toArray());
    //return new COMPAXSet(buffer);
  }

  public static int compareInts(int x, int y)
  {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public static COMPAXSet union(COMPAXSet... sets)
  {
    return union(Arrays.asList(sets));
  }

  public static COMPAXSet union(Iterable<COMPAXSet> sets)
  {
    return union(sets.iterator());
  }

  public static COMPAXSet union(Iterator<COMPAXSet> sets)
  {
	  COMPAXSet partialResults = doUnion(Iterators.limit(sets, CHUNK_SIZE));
    while (sets.hasNext()) {
      final UnmodifiableIterator<COMPAXSet> partialIter = Iterators.singletonIterator(partialResults);
      partialResults = doUnion(Iterators.<COMPAXSet>concat(partialIter, Iterators.limit(sets, CHUNK_SIZE)));
    }
    return partialResults;
  }

  public static COMPAXSet intersection(COMPAXSet... sets)
  {
    return intersection(Arrays.asList(sets));
  }

  public static COMPAXSet intersection(Iterable<COMPAXSet> sets)
  {
    return intersection(sets.iterator());
  }

  public static COMPAXSet intersection(Iterator<COMPAXSet> sets)
  {
    COMPAXSet partialResults = doIntersection(Iterators.limit(sets, CHUNK_SIZE));
    while (sets.hasNext()) {
      final UnmodifiableIterator<COMPAXSet> partialIter = Iterators.singletonIterator(partialResults);
      partialResults = doIntersection(
          Iterators.<COMPAXSet>concat(Iterators.limit(sets, CHUNK_SIZE), partialIter)
      );
    }
    return partialResults;
  }

  public static COMPAXSet complement(COMPAXSet set)
  {
    return doComplement(set);
  }

  public static COMPAXSet complement(COMPAXSet set, int length)
  {
    if (length <= 0) {
      return new COMPAXSet();
    }

    // special case when the set is empty and we need a concise set of ones
    if (set == null || set.isEmpty()) {
      ConciseSet newSet = new ConciseSet();
      for (int i = 0; i < length; i++) {
        newSet.add(i);
      }
      return COMPAXSet.newImmutableFromMutable(newSet);
    }

    IntList retVal = new IntList();
    int endIndex = length - 1;

    int wordsWalked = 0;
    int last = 0;

    WordIterator iter = set.newWordIterator();

    while (iter.hasNext()) {
        int word = iter.next();
        wordsWalked = iter.wordsWalked;
        if (isLiteral(word)) {
          retVal.add(ALL_ZEROS_LITERAL | ~word);
        } else {
      	  if(is0_fill(word))
      	  {
      		  retVal.add(SEQUENCE_1_FILL | getSequenceCount(word));
      	  }
      	  else
      	  {
      		  retVal.add(getSequenceCount(word));
      	  }
          //retVal.add(SEQUENCE_BIT ^ word);
        }
      }

    last = set.getLast();

    int distFromLastWordBoundary = maxLiteralLengthModulus(last);
    int distToNextWordBoundary = MAX_LITERAL_LENGTH - distFromLastWordBoundary - 1;
    last = (last < 0) ? 0 : last + distToNextWordBoundary;

    int diff = endIndex - last;
    // only append a new literal when the end index is beyond the current word
    if (diff > 0) {
      // first check if the difference can be represented in 31 bits
        // create a fill from last set bit to endIndex for number of 31 bit blocks minus one
    	if (diff <= MAX_LITERAL_LENGTH) {
            retVal.add(ALL_ONES_LITERAL);
          } else {
            // create a fill from last set bit to endIndex for number of 31 bit blocks minus one
            int endIndexWordCount = maxLiteralLengthDivision(endIndex);
            retVal.add(SEQUENCE_1_FILL | (endIndexWordCount - wordsWalked));
            retVal.add(ALL_ONES_LITERAL);
          }
    	}

    // clear bits after last set value
    int lastWord = retVal.get(retVal.length() - 1);
    if (isLiteral(lastWord)) {
        lastWord = clearBitsAfterInLastWord(
            lastWord,
            maxLiteralLengthModulus(endIndex)
        );
      }

    retVal.set(retVal.length() - 1, lastWord);
    trimZeros(retVal);

    if (retVal.isEmpty()) {
      return new COMPAXSet();
    }
    return compact(new COMPAXSet(IntBuffer.wrap(retVal.toArray())));
  }

  public static COMPAXSet compact(COMPAXSet set)
  {
    IntList retVal = new IntList();
    WordIterator itr = set.newWordIterator();
    while (itr.hasNext()) {
      addAndCompact(retVal, itr.next());
    }
    IntBuffer buffer =  IntBuffer.wrap(retVal.toArray());
    return new COMPAXSet(buffer, true);
  }
  
  public static int ConvertFill(int word)
  {
	  if(isOneSequence(word))
	  {
		  word &= 0x1fffffff;
		  word |= SEQUENCE_1_FILL;
		  word += 1;
	  }
	  else
	  {
		  if(isZeroSequence(word))
		  {
			  word &= 0x1fffffff;
			  word += 1;
		  }
		  else
		  {
			  if(isAllOnesLiteral(word))
			  {
				  word = SEQUENCE_1_FILL|1;
			  }
			  else
			  {
				  if(isAllZerosLiteral(word))
				  {
					  word = 0x00000001;
				  }
			  }
		  }
	  }
	  return word;
  }
  private static void addAndCompact(IntList set, int wordToAdd)		
  {
    int length = set.length();
   // wordToAdd = ConvertFill(wordToAdd);
    if (set.isEmpty()) {
      set.add(wordToAdd);
      return;
    }
    
    int last = set.get(length - 1);

    int newWord = 0;
    if(is0_fill(wordToAdd))
    {					//2 0-fll compressed
    	int fillcount = getSequenceCount(wordToAdd);
    	if(is0_fill(last))
    	{
    		{
    			if(getSequenceCount(wordToAdd) + getSequenceCount(last) <= MAX_FILL_NUM)
    			{
    				newWord = getSequenceCount(wordToAdd) + getSequenceCount(last);
    					//newWord |= SEQUENCE_1_FILL;
    				set.set(length - 1, newWord);
    				return;
    			}
    		}
    	}
    	else
    	{				//0-fill is compressed into a f-l-f
    		if(isF1_L_F2(last))
    		{
    			int[] fillnum = getFLFFILLWords(last);
    			//int filltype = (last & 0x04000000) >>> 26;
      			
      				if(fillnum[1] + getSequenceCount(wordToAdd) <= MAX_F_L_F_NUM)
      				{
      					newWord = last + getSequenceCount(wordToAdd);
      					set.set(length - 1, newWord);
      					return;
      				}
    		}
    		else
    		{
    			if(length > 1)
    			{
    				int pre_last = set.get(length - 2);
    				if(is0_fill(pre_last))
    				{
    					int fillnum = getSequenceCount(pre_last);
    					//if(isDirtyByte0Word(last) || isDirtyByte1Word(last))
    					if(isDirtyByte0Word(last))	
    					{
    						int literalPos = isDirtyByte0Word(last) ? getDirtyByte0Pos(last) : getDirtyByte1Pos(last);
    						int literalByte = isDirtyByte0Word(last) ? getDirty0(last) : getDirty1(last);
    						if(fillnum <= MAX_F_L_F_NUM && fillcount <= MAX_F_L_F_NUM)
    						{
    							//newWord = SEQUENCE_F1_L_F2 | fillcount | fillnum << 16 | literalByte << 8
    							//| literaltype << 27 | literalPos << 24 | fillkind << 26 | filltype << 28;
    							newWord = (SEQUENCE_F1_L_F2) | (fillcount) | (fillnum << 16) | (literalByte << 8)
    							 | (literalPos << 25);
    							set.set(length - 2, newWord);
    							set.setLength(length - 2);
    							return;	
    						}
    					}
    				}
    			}
    		}
    	}
    }
    else
    {
    	if(is1_fill(wordToAdd))
    	{
	    	if(is1_fill(last))
	    	{
    			if(getSequenceCount(wordToAdd) + getSequenceCount(last) <= MAX_FILL_NUM)
    			{
    				newWord = getSequenceCount(wordToAdd) + getSequenceCount(last);
    					//newWord |= SEQUENCE_1_FILL;
    				newWord |= SEQUENCE_1_FILL;
    				set.set(length - 1, newWord);
    				return;
    			}
	    	}
    	}
    	else
    	if(length > 1)
    	{
    		int pre_last = set.get(length - 2);
    		//if(isDirtyByte0Word(wordToAdd) || isDirtyByte1Word(wordToAdd))
    		if(isDirtyByte0Word(wordToAdd))
    		{
    			int literalPos2 = isDirtyByte0Word(wordToAdd) ? getDirtyByte0Pos(wordToAdd) : getDirtyByte1Pos(wordToAdd);
    			int literalByte2 = isDirtyByte0Word(wordToAdd) ? getDirty0(wordToAdd) : getDirty1(wordToAdd);
    			if(is0_fill(last))
    			{
    				int fillnum = getSequenceCount(last);
    				if(fillnum <= MAX_L_F_L_NUM)
    				{
    					//if(isDirtyByte0Word(pre_last) || isDirtyByte1Word(pre_last))
    					if(isDirtyByte0Word(pre_last))
    					{
    		    			int literalPos1 = isDirtyByte0Word(pre_last) ? getDirtyByte0Pos(pre_last) : getDirtyByte1Pos(pre_last);
    		    			int literalByte1 = isDirtyByte0Word(pre_last) ? getDirty0(pre_last) : getDirty1(pre_last);
    		    			if(isDirtyByte0Word(pre_last) && isDirtyByte0Word(wordToAdd))
    		    			{
    		    				newWord = (SEQUENCE_0L1_F_0L2) | (literalByte1 << 8) | (literalByte2) | (fillnum << 16)
    		    					| (literalPos1 << 27) | (literalPos2 << 25);
    		    				set.set(length - 2, newWord);
        						set.setLength(length - 2);
        						return;	
    		    			}
    		    			/*else
    		    			{
    		    				if(literaltype1 == 1 && literaltype2 == 0)
        		    			{
        		    				newWord = SEQUENCE_1L_F_0L | literalByte1 << 8 | literalByte2 | fillnum << 16
        		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
        		    				set.set(length - 2, newWord);
            						set.setLength(length - 2);
            						return;	
        		    			}
    		    				else
    		    				{
    		    					if(literaltype1 == 0 && literaltype2 == 1)
    	    		    			{
    	    		    				newWord = SEQUENCE_0L_F_1L | literalByte1 << 8 | literalByte2 | fillnum << 16
    	    		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
    	    		    				set.set(length - 2, newWord);
    	        						set.setLength(length - 2);
    	        						return;	
    	    		    			}
    		    					else
    		    					{
    		    						if(literaltype1 == 1 && literaltype2 == 1)
    		    		    			{
    		    		    				newWord = SEQUENCE_1L1_F_1L2 | literalByte1 << 8 | literalByte2 | fillnum << 16
    		    		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
    		    		    				set.set(length - 2, newWord);
    		        						set.setLength(length - 2);
    		        						return;	
    		    		    			}
    		    					}
    		    				}
    		    			}*/
    					}
    				}
    			}
    		}
    	}
    }
    /*if (isAllOnesLiteral(last)) {
      if (isAllOnesLiteral(wordToAdd)) {
        newWord = 0x10000001;
      } else if (isOneSequence(wordToAdd) && getFlippedBit(wordToAdd) == -1) {
        newWord = wordToAdd + 1;
      }
    } else if (isOneSequence(last)) {
      if (isAllOnesLiteral(wordToAdd)) {
        newWord = last + 1;
      } else if (isOneSequence(wordToAdd) && getFlippedBit(wordToAdd) == -1) {
        newWord = last + getSequenceNumWords(wordToAdd);
      }
    } else if (isAllZerosLiteral(last)) {
      if (isAllZerosLiteral(wordToAdd)) {
        newWord = 0x00000001;
      } else if (isZeroSequence(wordToAdd) && getFlippedBit(wordToAdd) == -1) {
        newWord = wordToAdd + 1;
      }
    } else if (isZeroSequence(last)) {
      if (isAllZerosLiteral(wordToAdd)) {
        newWord = last + 1;
      } else if (isZeroSequence(wordToAdd) && getFlippedBit(wordToAdd) == -1) {
        newWord = last + getSequenceNumWords(wordToAdd);
      }
    } else if (isLiteralWithSingleOneBit(last)) {
      int position = Integer.numberOfTrailingZeros(last) + 1;
      if (isAllZerosLiteral(wordToAdd)) {
        newWord = 0x00000001 | (position << 25);
      } else if (isZeroSequence(wordToAdd) && getFlippedBit(wordToAdd) == -1) {
        newWord = (wordToAdd + 1) | (position << 25);
      }
    } else if (isLiteralWithSingleZeroBit(last)) {
      int position = Integer.numberOfTrailingZeros(~last) + 1;
      if (isAllOnesLiteral(wordToAdd)) {
        newWord = 0x40000001 | (position << 25);
      } else if (isOneSequence(wordToAdd) && getFlippedBit(wordToAdd) == -1) {
        newWord = (wordToAdd + 1) | (position << 25);
      }
    }*/

    
      set.add(wordToAdd);
  }

  private static COMPAXSet doUnion(Iterator<COMPAXSet> iterator)
  {
    IntList retVal = new IntList();

    // lhs = current word position, rhs = the iterator
    // Comparison is first by index, then one fills > literals > zero fills
    // one fills are sorted by length (longer one fills have priority)
    // similarily, shorter zero fills have priority
    MinMaxPriorityQueue<WordHolder> theQ = MinMaxPriorityQueue.orderedBy(
        new Comparator<WordHolder>()
        {
          @Override
          public int compare(WordHolder h1, WordHolder h2)
          {
            int w1 = h1.getWord();
            int w2 = h2.getWord();
            int s1 = h1.getIterator().startIndex;
            int s2 = h2.getIterator().startIndex;

            if (s1 != s2) {
              return compareInts(s1, s2);
            }

            if (is1_fill(w1)) {
              if (is1_fill(w2)) {
                return -compareInts(getSequenceNumWords(w1), getSequenceNumWords(w2));
              }
              return -1;
            } else if (isLiteral(w1)) {
              if (is1_fill(w2)) {
                return 1;
              } else if (isLiteral(w2)) {
                return 0;
              }
              return -1;
            } else {
              if (!is0_fill(w2)) {
                return 1;
              }
              return compareInts(getSequenceNumWords(w1), getSequenceNumWords(w2));
            }
          }
        }
    ).create();

    // populate priority queue
    while (iterator.hasNext()) {
      COMPAXSet set = iterator.next();

      if (set != null && !set.isEmpty()) {
        WordIterator itr = set.newWordIterator();
        theQ.add(new WordHolder(itr.next(), itr));
      }
    }
    
    int currIndex = 0;

    while (!theQ.isEmpty()) {
      // create a temp list to hold everything that will get pushed back into the priority queue after each run
      List<WordHolder> wordsToAdd = Lists.newArrayList();

      // grab the top element from the priority queue
      WordHolder curr = theQ.poll();
      int word = curr.getWord();
      WordIterator itr = curr.getIterator();

      // if the next word in the queue starts at a different point than where we ended off we need to create a zero gap
      // to fill the space
      if (currIndex < itr.startIndex) {
        addAndCompact(retVal, itr.startIndex - currIndex);
        currIndex = itr.startIndex;
      }

      if (is1_fill(word)) {
        // extract a literal from the flip bits of the one sequence
        //int flipBitLiteral = getLiteralFromOneSeqFlipBit(word);

        // advance everything past the longest ones sequence
        WordHolder nextVal = theQ.peek();
        while (nextVal != null &&
               nextVal.getIterator().startIndex < itr.wordsWalked) {
          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();

          /*if (i.startIndex == itr.startIndex) {
            // if a literal was created from a flip bit, OR it with other literals or literals from flip bits in the same
            // position
            if (isOneSequence(w)) {
              flipBitLiteral |= getLiteralFromOneSeqFlipBit(w);
            } else if (isLiteral(w)) {
              flipBitLiteral |= w;
            } else {
              flipBitLiteral |= getLiteralFromZeroSeqFlipBit(w);
            }
          }*/

          i.advanceTo(itr.wordsWalked);
          if (i.hasNext()) {
            wordsToAdd.add(new WordHolder(i.next(), i));
          }
          nextVal = theQ.peek();
        }

        // advance longest one literal forward and push result back to priority queue
        // if a flip bit is still needed, put it in the correct position
        /*int newWord = word & 0xC1FFFFFF;
        if (flipBitLiteral != ALL_ONES_LITERAL) {
          flipBitLiteral ^= ALL_ONES_LITERAL;
          int position = Integer.numberOfTrailingZeros(flipBitLiteral) + 1;
          newWord |= (position << 25);
        }*/
        addAndCompact(retVal, word);
        currIndex = itr.wordsWalked;

        if (itr.hasNext()) {
          wordsToAdd.add(new WordHolder(itr.next(), itr));
        }
      } else if (isLiteral(word)) {
        // advance all other literals
        WordHolder nextVal = theQ.peek();
        while (nextVal != null &&
               nextVal.getIterator().startIndex == itr.startIndex) {

          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();

          // if we still have zero fills with flipped bits, OR them here
          if (isLiteral(w)) {
            word |= w;
            if(word == 0xffffffff)
            	word = SEQUENCE_1_FILL|1;
          } else {
            /*int flipBitLiteral = getLiteralFromZeroSeqFlipBit(w);
            if (flipBitLiteral != ALL_ZEROS_LITERAL) {
              word |= flipBitLiteral;
              i.advanceTo(itr.wordsWalked);
            }*/
        	if(is1_fill(w))
        	{
        		word = SEQUENCE_1_FILL|1;
        		i.advanceTo(itr.wordsWalked);
        	}
          }

          if (i.hasNext()) {
            wordsToAdd.add(new WordHolder(i.next(), i));
          }

          nextVal = theQ.peek();
        }

        // advance the set with the current literal forward and push result back to priority queue
        addAndCompact(retVal, word);
        currIndex++;

        if (itr.hasNext()) {
          wordsToAdd.add(new WordHolder(itr.next(), itr));
        }
      } else { // zero fills
          WordHolder nextVal = theQ.peek();

          while (nextVal != null &&
                 nextVal.getIterator().startIndex == itr.startIndex) {
            // check if literal can be created flip bits of other zero sequences
            WordHolder entry = theQ.poll();
            int w = entry.getWord();
            WordIterator i = entry.getIterator();

            if (i.hasNext()) {
              wordsToAdd.add(new WordHolder(i.next(), i));
            }
            nextVal = theQ.peek();
          }

          // check if a literal needs to be created from the flipped bits of this sequence
          if (itr.hasNext()) {
            wordsToAdd.add(new WordHolder(itr.next(), itr));
          }
          
        }

      theQ.addAll(wordsToAdd);
    }

    if (retVal.isEmpty()) {
      return new COMPAXSet();
    }
    return new COMPAXSet(IntBuffer.wrap(retVal.toArray()),true);
  }

  public static COMPAXSet doIntersection(Iterator<COMPAXSet> sets)
  {
    IntList retVal = new IntList();

    // lhs = current word position, rhs = the iterator
    // Comparison is first by index, then zero fills > literals > one fills
    // zero fills are sorted by length (longer zero fills have priority)
    // similarily, shorter one fills have priority
    MinMaxPriorityQueue<WordHolder> theQ = MinMaxPriorityQueue.orderedBy(
        new Comparator<WordHolder>()
        {
          @Override
          public int compare(WordHolder h1, WordHolder h2)
          {
            int w1 = h1.getWord();
            int w2 = h2.getWord();
            int s1 = h1.getIterator().startIndex;
            int s2 = h2.getIterator().startIndex;

            if (s1 != s2) {
              return compareInts(s1, s2);
            }

            if (is0_fill(w1)) {
              if (is0_fill(w2)) {
                return -compareInts(getSequenceNumWords(w1), getSequenceNumWords(w2));
              }
              return -1;
            } else if (isLiteral(w1)) {
              if (is0_fill(w2)) {
                return 1;
              } else if (isLiteral(w2)) {
                return 0;
              }
              return -1;
            } else {
              if (!is1_fill(w2)) {
                return 1;
              }
              return compareInts(getSequenceNumWords(w1), getSequenceNumWords(w2));
            }
          }
        }
    ).create();

    // populate priority queue
    while (sets.hasNext()) {
      COMPAXSet set = sets.next();

      if (set == null || set.isEmpty()) {
        return new COMPAXSet();
      }

      WordIterator itr = set.newWordIterator();
      theQ.add(new WordHolder(itr.next(), itr));
    }

    int currIndex = 0;
    int wordsWalkedAtSequenceEnd = Integer.MAX_VALUE;

    while (!theQ.isEmpty()) {
      // create a temp list to hold everything that will get pushed back into the priority queue after each run
      List<WordHolder> wordsToAdd = Lists.newArrayList();

      // grab the top element from the priority queue
      WordHolder curr = theQ.poll();
      int word = curr.getWord();
      WordIterator itr = curr.getIterator();

      // if a sequence has ended, we can break out because of Boolean logic
      if (itr.startIndex >= wordsWalkedAtSequenceEnd) {
        break;
      }

      // if the next word in the queue starts at a different point than where we ended off we need to create a one gap
      // to fill the space
      if (currIndex < itr.startIndex) {
        // number of 31 bit blocks that compromise the fill minus one
        addAndCompact(retVal, (SEQUENCE_1_FILL | (itr.startIndex - currIndex)));
        currIndex = itr.startIndex;
      }

      if (is0_fill(word)) {
        // extract a literal from the flip bits of the zero sequence
        //int flipBitLiteral = getLiteralFromZeroSeqFlipBit(word);

        // advance everything past the longest zero sequence
        WordHolder nextVal = theQ.peek();
        while (nextVal != null &&
               nextVal.getIterator().startIndex < itr.wordsWalked) {
          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();

          /*if (i.startIndex == itr.startIndex) {
            // if a literal was created from a flip bit, AND it with other literals or literals from flip bits in the same
            // position
            if (isZeroSequence(w)) {
              flipBitLiteral &= getLiteralFromZeroSeqFlipBit(w);
            } else if (isLiteral(w)) {
              flipBitLiteral &= w;
            } else {
              flipBitLiteral &= getLiteralFromOneSeqFlipBit(w);
            }
          }*/

          i.advanceTo(itr.wordsWalked);
          if (i.hasNext()) {
            wordsToAdd.add(new WordHolder(i.next(), i));
          } else {
            wordsWalkedAtSequenceEnd = Math.min(i.wordsWalked, wordsWalkedAtSequenceEnd);
          }
          nextVal = theQ.peek();
        }

        // advance longest zero literal forward and push result back to priority queue
        // if a flip bit is still needed, put it in the correct position
        //int newWord = word & 0xC1FFFFFF;
        /*if (flipBitLiteral != ALL_ZEROS_LITERAL) {
          int position = Integer.numberOfTrailingZeros(flipBitLiteral) + 1;
          newWord = (word & 0xC1FFFFFF) | (position << 25);
        }*/
        addAndCompact(retVal, word);
        currIndex = itr.wordsWalked;

        if (itr.hasNext()) {
          wordsToAdd.add(new WordHolder(itr.next(), itr));
        } else {
          wordsWalkedAtSequenceEnd = Math.min(itr.wordsWalked, wordsWalkedAtSequenceEnd);
        }
      } else if (isLiteral(word)) {
        // advance all other literals
        WordHolder nextVal = theQ.peek();
        while (nextVal != null &&
               nextVal.getIterator().startIndex == itr.startIndex) {

          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();

          // if we still have one fills with flipped bits, AND them here
          if (isLiteral(w)) {
            word &= w;
            if(word == 0x80000000)
            {
            	word = 0x1;
            }
          } else {
            /*int flipBitLiteral = getLiteralFromOneSeqFlipBit(w);
            if (flipBitLiteral != ALL_ONES_LITERAL) {
              word &= flipBitLiteral;
              i.advanceTo(itr.wordsWalked);
            }*/
        	if(is0_fill(w))
        	{
        		word = 0x00000001;
        		i.advanceTo(itr.wordsWalked);
        	}
        	else
        	{
        		i.advanceTo(itr.wordsWalked);
        	}
          }

          if (i.hasNext()) {
            wordsToAdd.add(new WordHolder(i.next(), i));
          } else {
            wordsWalkedAtSequenceEnd = Math.min(i.wordsWalked, wordsWalkedAtSequenceEnd);
          }

          nextVal = theQ.peek();
        }

        // advance the set with the current literal forward and push result back to priority queue
        addAndCompact(retVal, word);
        currIndex++;

        if (itr.hasNext()) {
          wordsToAdd.add(new WordHolder(itr.next(), itr));
        } else {
          wordsWalkedAtSequenceEnd = Math.min(itr.wordsWalked, wordsWalkedAtSequenceEnd);
        }
      } else { // one fills
        //int flipBitLiteral;
        WordHolder nextVal = theQ.peek();

        while (nextVal != null &&
               nextVal.getIterator().startIndex == itr.startIndex) {
          // check if literal can be created flip bits of other one sequences
          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();
          i.advanceTo(itr.wordsWalked);
          /*flipBitLiteral = getLiteralFromOneSeqFlipBit(w);
          if (flipBitLiteral != ALL_ONES_LITERAL) {
            wordsToAdd.add(new WordHolder(flipBitLiteral, i));
          } else */if (i.hasNext()) {
            wordsToAdd.add(new WordHolder(i.next(), i));
          } else {
            wordsWalkedAtSequenceEnd = Math.min(i.wordsWalked, wordsWalkedAtSequenceEnd);
          }

          nextVal = theQ.peek();
        }

        // check if a literal needs to be created from the flipped bits of this sequence
        //flipBitLiteral = getLiteralFromOneSeqFlipBit(word);
        /*if (flipBitLiteral != ALL_ONES_LITERAL) {
          wordsToAdd.add(new WordHolder(flipBitLiteral, itr));
        } else */if (itr.hasNext()) {
          wordsToAdd.add(new WordHolder(itr.next(), itr));
        } else {
          wordsWalkedAtSequenceEnd = Math.min(itr.wordsWalked, wordsWalkedAtSequenceEnd);
        }
      }

      theQ.addAll(wordsToAdd);
    }

    // fill in any missing one sequences
    if (currIndex < wordsWalkedAtSequenceEnd) {
      addAndCompact(retVal, (SEQUENCE_1_FILL | (wordsWalkedAtSequenceEnd - currIndex)));
    }

    if (retVal.isEmpty()) {
      return new COMPAXSet();
    }
    return new COMPAXSet(IntBuffer.wrap(retVal.toArray()),true);
  }

  public static COMPAXSet doComplement(COMPAXSet set)
  {
    if (set == null || set.isEmpty()) {
      return new COMPAXSet();
    }

    IntList retVal = new IntList();
    WordIterator iter = set.newWordIterator();
    while (iter.hasNext()) {
      int word = iter.next();
      if (isLiteral(word)) {
        retVal.add(ALL_ZEROS_LITERAL | ~word);
      } else {
    	  if(is0_fill(word))
    	  {
    		  retVal.add(SEQUENCE_1_FILL | getSequenceCount(word));
    	  }
    	  else
    	  {
    		  retVal.add(getSequenceCount(word));
    	  }
        //retVal.add(SEQUENCE_BIT ^ word);
      }
    }
    // do not complement after the last element
    int lastWord = retVal.get(retVal.length() - 1);
    if (isLiteral(lastWord)) {
      lastWord = clearBitsAfterInLastWord(
          lastWord,
          maxLiteralLengthModulus(set.getLast())
      );
    }

    retVal.set(retVal.length() - 1, lastWord);

    trimZeros(retVal);

    if (retVal.isEmpty()) {
      return new COMPAXSet();
    }
    return new COMPAXSet(IntBuffer.wrap(retVal.toArray()),true);
  }

  // Based on the ConciseSet implementation by Alessandro Colantonio
  private static void trimZeros(IntList set)
  {
    // loop over ALL_ZEROS_LITERAL words
    int w;
    int last = set.length() - 1;
    do {
      w = set.get(last);
      if(is0_fill(w))
    	  {
    	  	set.set(last, 0);
    	  	last--;
    	  }
      else {
        // one sequence or literal
        return;
      }
      if (set.isEmpty() || last == -1) {
        return;
      }
    } while (true);
  }

  private final IntBuffer words;
  private final int lastWordIndex;
  private final int size;
  
  public int[] getWords()
  {
	  return words.array();
  }
  public COMPAXSet()
  {
    this.words = null;
    this.lastWordIndex = -1;
    this.size = 0;
  }

  public COMPAXSet(ByteBuffer byteBuffer)
  {
    this.words = byteBuffer.asIntBuffer();
    int[] word = words.array();
    for(int i = 0;i<word.length ; i++)
    {
    	word[i] = ConvertFill(word[i]);
    }
    
    words.put(word);
    this.lastWordIndex = words.capacity() - 1;
    this.size = calcSize();
  }
  public COMPAXSet(IntBuffer buffer, boolean iscompax)
  {
    this.words = buffer;
    int[] word = words.array();
    this.lastWordIndex = (words == null || buffer.capacity() == 0) ? -1 : words.capacity() - 1;
    this.size = calcSize();
  }
  public COMPAXSet(IntBuffer buffer)
  {
    this.words = buffer;
    int[] word = words.array();
    for(int i = 0;i<word.length ; i++)
    {
    	word[i] = ConvertFill(word[i]);
    }
    int [] result_word;
    
    
    words.put(word);
    this.lastWordIndex = (words == null || buffer.capacity() == 0) ? -1 : words.capacity() - 1;
    this.size = calcSize();
  }

  public byte[] toBytes()
  {
    ByteBuffer buf = ByteBuffer.allocate(words.capacity() * Ints.BYTES);
    buf.asIntBuffer().put(words.asReadOnlyBuffer());
    return buf.array();
  }

  public int getLastWordIndex()
  {
    return lastWordIndex;
  }
  
  public int [] getSeq(int m)
  {
	  ArrayList<Integer> list = new ArrayList<Integer>();
	  WordIterator itr = this.newWordIterator();
	  int w = 0;
	  int num = 0;
	  while(itr.hasNext())
	  {
		  w = itr.next();
		  if(is0_fill(w))
		  {
			  num += w;
		  }
		  else
		  {
			  if(is1_fill(w))
			  {
				  int count = w & 0x0fffffff;
				  for(int i = 0; i<count * 31; i++)
				  {
					  list.add(num * 31 + i);
					  if(150 <= list.size())
					  {
						  int y = 0;
						  y++;
					  }
				  }
				  num += count;
			  }
			  else
			  {
			            long bitset = w & 0x7fffffff;
			            while (bitset != 0) {
			                long t = bitset & -bitset;
			               list.add((31 * num + Long.bitCount(t - 1)));
			                bitset ^= t;
			            }
			            if(150 <= list.size())
						  {
							  int y = 0;
							  y++;
						  }
			            
			            num++;
			        
			  }
		  }
	  }
	  
	  int [] res = new int[list.size()];
	  for(int k = 0; k<list.size(); k++)
	  {
		  res[k] = list.get(k);
	  }
	  return res;
  }

  // Based on the ConciseSet implementation by Alessandro Colantonio
  private int calcSize()  
  {
    int retVal = 0;
    for (int i = 0; i <= lastWordIndex; i++) {
      int w = words.get(i);
      if (isLiteral(w)) {
        retVal += getLiteralBitCount(w);
      } else {
        if (is0_fill(w)) {
            retVal += 0;
        } else {
          if(is1_fill(w))
        	  retVal += maxLiteralLengthMultiplication(getSequenceCount(w));
          else
          {
        	  if(isF1_L_F2(w))
        	  {
        		  int[] fillnum = getFLFFILLWords(w);
        		  int literalByte = getFLFLiteralWords(w);
        		  //int literaltype = (w & 0x08000000) >>> 27;
    			  int num;
    			  //if(literaltype == 0)
    			 // {
    				  num = getLiteralBitCount(literalByte);
    			  //}
    			 /* else
    			  {
    				  num = getLiteralBitCount(literalByte) + 23;
    			  }*/
        		  retVal += maxLiteralLengthMultiplication(fillnum[0] + fillnum[1])
        		  	+ num;
        	  }
        	  else
        	  {
        		  if(is0L1_F_0L2(w))
        		  {
        			  int fillnum = getLFLFILLWords(w);
        			  //int filltype = (w & 0x00800000) >>> 23;
        			  int[] literalByte = getLFLLiteralWords(w);
        			  int[]literalnum = new int [2];
        			  literalnum[0] = getLiteralBitCount(literalByte[0]);
        			  literalnum[1] = getLiteralBitCount(literalByte[1]);
        			  retVal += maxLiteralLengthMultiplication(fillnum) + literalnum[0]
        			       + literalnum[1];
        		  }
        		  /*else
        		  {
        			  if(is1L1_F_1L2(w))
            		  {
            			  int fillnum = getLFLFILLWords(w);
            			  int filltype = (w & 0x00800000) >>> 23;
            			  int[] literalByte = getLFLLiteralWords(w);
            			  int[]literalnum = new int [2];
            			  literalnum[0] = getLiteralBitCount(literalByte[0]) + 23;
            			  literalnum[1] = getLiteralBitCount(literalByte[1]) + 23;
            			  retVal += maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
            			       + literalnum[1];
            		  }
        			  else
        			  {
        				  if(is0L_F_1L(w))
                		  {
                			  int fillnum = getLFLFILLWords(w);
                			  int filltype = (w & 0x00800000) >>> 23;
                			  int[] literalByte = getLFLLiteralWords(w);
                			  int[]literalnum = new int [2];
                			  literalnum[0] = getLiteralBitCount(literalByte[0]);
                			  literalnum[1] = getLiteralBitCount(literalByte[1]) + 23;
                			  retVal += maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
                			       + literalnum[1];
                		  }
        				  else
        				  {
        					  if(is1L_F_0L(w))
        	        		  {
        	        			  int fillnum = getLFLFILLWords(w);
        	        			  int filltype = (w & 0x00800000) >>> 23;
        	        			  int[] literalByte = getLFLLiteralWords(w);
        	        			  int[]literalnum = new int [2];
        	        			  literalnum[0] = getLiteralBitCount(literalByte[0]) + 23;
        	        			  literalnum[1] = getLiteralBitCount(literalByte[1]);
        	        			  retVal += maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
        	        			       + literalnum[1];
        	        		  }
        				  }
        			  }
        		  }*/
        	  }
          }
        }
      }
    }

    return retVal;
  }

  public int size()
  {
    return size;
  }

  // Based on the ConciseSet implementation by Alessandro Colantonio
  public int getLast()
  {
    if (isEmpty()) {
      return -1;
    }

    int last = 0;
    for (int i = 0; i <= lastWordIndex; i++) {
      int w = words.get(i);
      if (isLiteral(w)) {
        last += MAX_LITERAL_LENGTH;
      } else {
    	  if(is0_fill(w) || is1_fill(w))
    		  last += maxLiteralLengthMultiplication(getSequenceCount(w));
    	  else
    	  {
    		  if(isF1_L_F2(w))
    		  {
    			  int[] fillnum = getFLFFILLWords(w);
    			  last += maxLiteralLengthMultiplication(fillnum[0] + fillnum[1] + 1);
    		  }
    		  else
    		  {
    			//  if(is0L1_F_0L2(w) || is0L_F_1L(w) 
    			//		  || is1L1_F_1L2(w) || is1L_F_0L(w))
				  if(is0L1_F_0L2(w))
    			  {
    				  int fillnum = getLFLFILLWords(w);
    				  last += maxLiteralLengthMultiplication(fillnum + 2);
    			  }
    				  
    		  }
    	  }
      }
    }

    int w = words.get(lastWordIndex);
    if (isLiteral(w)) {
      last -= Integer.numberOfLeadingZeros(getLiteralBits(w));
    } else {
    	if(is1_fill(w))
    		last--;
    	else
    	{
    		//if(is0L1_F_0L2(w) || is0L_F_1L(w) 
    		//		|| is1L1_F_1L2(w) || is1L_F_0L(w))
    		if(is0L1_F_0L2(w))
    		{
    			int[] fillByte = getLFLLiteralWords(w);
    			last -= Integer.numberOfLeadingZeros(getLiteralBits(fillByte[1]));
    		}
    	}
    }
    return last;
  }

  // Based on the ConciseSet implementation by Alessandro Colantonio
  public int get(int i)
  {
    if (i < 0) {
      throw new IndexOutOfBoundsException();
    }

    // initialize data
    int firstSetBitInWord = 0;
    int position = i;
    int setBitsInCurrentWord = 0;
    for (int j = 0; j <= lastWordIndex; j++) {
      int w = words.get(j);
      if (isLiteral(w)) {
        // number of bits in the current word
        setBitsInCurrentWord = getLiteralBitCount(w);

        // check if the desired bit is in the current word
        if (position < setBitsInCurrentWord) {
          int currSetBitInWord = -1;
          for (; position >= 0; position--) {
            currSetBitInWord = Integer.numberOfTrailingZeros(w & (0xFFFFFFFF << (currSetBitInWord + 1)));
          }
          return firstSetBitInWord + currSetBitInWord;
        }

        // skip the 31-bit block
        firstSetBitInWord += MAX_LITERAL_LENGTH;
      } else {
        // number of involved bits (31 * blocks)
        setBitsInCurrentWord = 0;
        // check the sequence type
        if (is1_fill(w) || is0_fill(w)) {
        	int sequenceLength = maxLiteralLengthMultiplication(getSequenceCount(w));
            if(is1_fill(w))
            	setBitsInCurrentWord = sequenceLength;
            if (position < setBitsInCurrentWord) 
            {
              return firstSetBitInWord + position;
            }
            firstSetBitInWord += sequenceLength;
        } else {
          if(isF1_L_F2(w))
          {
        	  int[]fillnum = getFLFFILLWords(w);
        	  //int literalType = (w & 0x08000000) >>> 27;
              int literalByte = getFLFLiteralWords(w);
             // int literalPos = (w & 0x03000000) >>> 24;
              int literalPos = (w & 0x06000000) >>> 25;
              //fillType[1] = (w & 0x04000000) >>> 26;
    	      setBitsInCurrentWord = maxLiteralLengthMultiplication(fillnum[0]);
    	      int sequencelength = maxLiteralLengthMultiplication(fillnum[0]);
    	      if(position < setBitsInCurrentWord)
    	      {
    	    	  return firstSetBitInWord + position;
    	      }
    	      firstSetBitInWord += sequencelength;
    	      position -= setBitsInCurrentWord;
    	      int literal;
    	     // if(literalType == 0)
    	     // {
    	    	  literal = 0x80000000 | (literalByte << (literalPos * 8));
    	     // }
    	     /* else
    	      {
    	    	  literal = 0x80000000 | (literalByte << (literalPos * 8));
    	    	  switch(literalPos)
    	    	  {
    	    	  case 0: literal |= 0xffffff00;break;
    	    	  case 1: literal |= 0xffff00ff;break;
    	    	  case 2: literal |= 0xff00ffff;break;
    	    	  case 3: literal |= 0x80ffffff;break;
    	    	  }
    	      }*/
    	      setBitsInCurrentWord = getLiteralBitCount(literal);
    	      if (position < setBitsInCurrentWord) {
    	          int currSetBitInWord = -1;
    	          for (; position >= 0; position--) {
    	            currSetBitInWord = Integer.numberOfTrailingZeros(literal & (0xFFFFFFFF << (currSetBitInWord + 1)));
    	          }
    	          return firstSetBitInWord + currSetBitInWord;
    	       }
    	      firstSetBitInWord += MAX_LITERAL_LENGTH;
    	      position -= setBitsInCurrentWord;
    	      
    	      setBitsInCurrentWord = maxLiteralLengthMultiplication(fillnum[1]);
    	      sequencelength = maxLiteralLengthMultiplication(fillnum[1]);
    	      if(position < setBitsInCurrentWord)
    	      {
    	    	  return firstSetBitInWord + position;
    	      }
    	      firstSetBitInWord += sequencelength;
          }
          else
          {
        	  if(is0L1_F_0L2(w))
        	  {
        		  int fillnum = getLFLFILLWords(w);
    	          int[] literalByte = getLFLLiteralWords(w);
    	          int[] literalPos = new int [2];
    	          literalPos[0] = (w & 0x18000000) >>> 27;
    	          literalPos[1] = (w & 0x06000000) >>> 25;
    	    	  int[] literal = new int [2];
    	    	  /*else
    	    	  {
    	    		  if(is1L1_F_1L2(w))
        	    	  {
        	    		  literalType[0] = 1;
        	    		  literalType[1] = 1;
        	    	  }
    	    		  else
    	    		  {
    	    			  if(is0L_F_1L(w))
    	    	    	  {
    	    	    		  literalType[0] = 0;
    	    	    		  literalType[1] = 1;
    	    	    	  }
    	    			  else
    	    			  {
    	    				  if(is1L_F_0L(w))
    	        	    	  {
    	        	    		  literalType[0] = 1;
    	        	    		  literalType[1] = 0;
    	        	    	  }
    	    			  }
    	    		  }
    	    	  }*/
        	      
        	    	  literal[0] = 0x80000000 | (literalByte[0] << (literalPos[0] * 8));
        	      
        	      /*else
        	      {
        	    	  literal[0] = 0x80000000 | (literalByte[0] << (literalPos[0] * 8));
        	    	  switch(literalPos[0])
        	    	  {
        	    	  case 0: literal[0] |= 0xffffff00;break;
        	    	  case 1: literal[0] |= 0xffff00ff;break;
        	    	  case 2: literal[0] |= 0xff00ffff;break;
        	    	  case 3: literal[0] |= 0x80ffffff;break;
        	    	  }
        	      }*/
        	      
        	    	  literal[1] = 0x80000000 | (literalByte[1] << (literalPos[1] * 8));
        	      
        	      /*else
        	      {
        	    	  literal[1] = 0x80000000 | (literalByte[1] << (literalPos[1] * 8));
        	    	  switch(literalPos[1])
        	    	  {
        	    	  case 0: literal[1] |= 0xffffff00;break;
        	    	  case 1: literal[1] |= 0xffff00ff;break;
        	    	  case 2: literal[1] |= 0xff00ffff;break;
        	    	  case 3: literal[1] |= 0x80ffffff;break;
        	    	  }
        	      }*/
    	          
    	          setBitsInCurrentWord = getLiteralBitCount(literal[0]);
        	      if (position < setBitsInCurrentWord) {
        	          int currSetBitInWord = -1;
        	          for (; position >= 0; position--) {
        	            currSetBitInWord = Integer.numberOfTrailingZeros(literal[0] & (0xFFFFFFFF << (currSetBitInWord + 1)));
        	          }
        	          return firstSetBitInWord + currSetBitInWord;
        	       }
        	      firstSetBitInWord += MAX_LITERAL_LENGTH;
        	      position -= setBitsInCurrentWord;
        	      
        	      setBitsInCurrentWord = maxLiteralLengthMultiplication(fillnum);
        	      int sequencelength = maxLiteralLengthMultiplication(fillnum);
        	      if(position < setBitsInCurrentWord)
        	      {
        	    	  return firstSetBitInWord + position;
        	      }
        	      firstSetBitInWord += sequencelength;
        	      position -= setBitsInCurrentWord;
        	      
        	      setBitsInCurrentWord = getLiteralBitCount(literal[1]);
        	      if (position < setBitsInCurrentWord) {
        	          int currSetBitInWord = -1;
        	          for (; position >= 0; position--) {
        	            currSetBitInWord = Integer.numberOfTrailingZeros(literal[1] & (0xFFFFFFFF << (currSetBitInWord + 1)));
        	          }
        	          return firstSetBitInWord + currSetBitInWord;
        	       }
        	      firstSetBitInWord += MAX_LITERAL_LENGTH;
        	  }
          }
        }

        // skip the 31-bit blocks
        
      }

      // update the number of found set bits
      position -= setBitsInCurrentWord;
    }

    throw new IndexOutOfBoundsException(Integer.toString(i));
  }

  public int compareTo(COMPAXSet other)
  {
    return words.asReadOnlyBuffer().compareTo(other.words.asReadOnlyBuffer());
  }

  private boolean isEmpty()
  {
    return words == null || words.array().length == 0;
  }

  @Override
  // Based on the AbstractIntSet implementation by Alessandro Colantonio
  public String toString()
  {
    IntSet.IntIterator itr = iterator();
    if (!itr.hasNext()) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (; ; ) {
      sb.append(itr.next());
      if (!itr.hasNext()) {
        return sb.append(']').toString();
      }
      sb.append(", ");
    }
  }

  // Based on the ConciseSet implementation by Alessandro Colantonio
  public IntSet.IntIterator iterator()
  {
    if (isEmpty()) {
      return new IntSet.IntIterator()
      {
        @Override
        public void skipAllBefore(int element) {/*empty*/}

        @Override
        public boolean hasNext() {return false;}

        @Override
        public int next() {throw new NoSuchElementException();}

        @Override
        public void remove() {throw new UnsupportedOperationException();}

        @Override
        public IntSet.IntIterator clone() {throw new UnsupportedOperationException();}
      };
    }
    
    //閿熸枻鎷�
    BitIterator bit = new BitIterator();
    return bit;
  }

  public WordIterator newWordIterator()
  {
	 // BitIterator.next();
    return new WordIterator();
  }
  /*private class BitIterator implements IntSet.IntIterator
  {
    final LiteralAndZeroFillExpander litExp;
    final OneFillExpander oneExp;

    WordExpander exp;
    int nextIndex = 0;
    int nextOffset = 0;

    private BitIterator()
    {
      litExp = newLiteralAndZeroFillExpander();
      oneExp = newOneFillExpander();

      nextWord();
    }

    private BitIterator(
        LiteralAndZeroFillExpander litExp,
        OneFillExpander oneExp,
        WordExpander exp,
        int nextIndex,
        int nextOffset
    )
    {
      this.litExp = litExp;
      this.oneExp = oneExp;
      this.exp = exp;
      this.nextIndex = nextIndex;
      this.nextOffset = nextOffset;
    }

    @Override
    public boolean hasNext()
    {
      while (!exp.hasNext()) {
        if (nextIndex > lastWordIndex) {
          return false;
        }
        nextWord();
      }
      return true;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return exp.next();
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void skipAllBefore(int element)
    {
      while (true) {
        exp.skipAllBefore(element);
        if (exp.hasNext() || nextIndex > lastWordIndex) {
          return;
        }
        nextWord();
      }
    }

    @Override
    public IntSet.IntIterator clone()
    {
      return new BitIterator(
          (LiteralAndZeroFillExpander) litExp.clone(),
          (OneFillExpander) oneExp.clone(),
          exp.clone(),
          nextIndex,
          nextOffset
      );
    }

    private void nextWord()
    {
      final int word = words.get(nextIndex++);
      exp = is1_fill(word) ? oneExp : litExp;
      exp.reset(nextOffset, word, true);

      // prepare next offset
      if (isLiteral(word)) {
        nextOffset += MAX_LITERAL_LENGTH;
      } else {
        nextOffset += maxLiteralLengthMultiplication(getSequenceCount(word));
      }
    }
  }
  */
  // Based on the ConciseSet implementation by Alessandro Colantonio
  private class BitIterator implements IntSet.IntIterator
  {
    final LiteralAndZeroFillExpander litExp;
    final OneFillExpander oneExp;
    final FLFExpander flfExp;
    final LFLExpander lflExp;

    WordExpander exp;
    int nextIndex = 0;
    int nextOffset = 0;

    private BitIterator()
    {
      litExp = newLiteralAndZeroFillExpander();
      oneExp = newOneFillExpander();
      flfExp = newFLFExpander();
      lflExp = newLFLExpander();
      nextWord();
    }

    private BitIterator(
        LiteralAndZeroFillExpander litExp,
        OneFillExpander oneExp,
        WordExpander exp,
        FLFExpander flfExp,
        LFLExpander lflExp,
        int nextIndex,
        int nextOffset
    )
    {
      this.litExp = litExp;
      this.oneExp = oneExp;
      this.exp = exp;
      this.nextIndex = nextIndex;
      this.nextOffset = nextOffset;
      this.flfExp = flfExp;
      this.lflExp = lflExp;
    }

    @Override
    public boolean hasNext()
    {
      while (!exp.hasNext()) {
        if (nextIndex > lastWordIndex) {
          return false;
        }
        nextWord();
      }
      return true;
    }

    @Override
    public int next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return exp.next();
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void skipAllBefore(int element)
    {
      while (true) {
        exp.skipAllBefore(element);
        if (exp.hasNext() || nextIndex > lastWordIndex) {
          return;
        }
        nextWord();
      }
    }

    @Override
    public IntSet.IntIterator clone()
    {
      return new BitIterator(
          (LiteralAndZeroFillExpander) litExp.clone(),
          (OneFillExpander) oneExp.clone(),
          exp.clone(),
          (FLFExpander) flfExp.clone(),
          (LFLExpander) lflExp.clone(),
          nextIndex,
          nextOffset
      );
    }

    private void nextWord()
    {
      final int word = words.get(nextIndex++);
      if(is1_fill(word))		//閿熸枻鎷�
    	  exp = oneExp;
      else
      {
    	  if(is0_fill(word) || isLiteral(word))
    	  {
    		  exp = litExp;
    	  }
    	  else
    	  {
    		  if(isF1_L_F2(word))
    		  {
    			  exp = flfExp;
    		  }
    		  else
    		  {
    			 // if(is0L1_F_0L2(word) || is0L_F_1L(word) 
    			//		  || is1L1_F_1L2(word) || is1L_F_0L(word))
    			  if(is0L1_F_0L2(word))
    			  {
    				  exp = lflExp;
    			  }
    		  }
    	  }
      }
      exp.reset(nextOffset, word, true);

      // prepare next offset
      if (isLiteral(word)) {
        nextOffset += MAX_LITERAL_LENGTH;
      } else {
    	  if(is0_fill(word) || is1_fill(word))
    		  nextOffset += maxLiteralLengthMultiplication(getSequenceCount(word));
    	  else
    	  {
    		  if(isF1_L_F2(word))
    		  {
    			  int[] fillnum = getFLFFILLWords(word);
    			  nextOffset += maxLiteralLengthMultiplication(fillnum[0] + fillnum[1] + 1);
    		  }
    		  else
    		  {
    			  //if(is1L1_F_1L2(word) || is1L_F_0L(word) 
    			//		  || is0L1_F_0L2(word) || is0L_F_1L(word))
        		  if(is0L1_F_0L2(word))
    			  {
    				  int fillnum = getLFLFILLWords(word);
    				  nextOffset += maxLiteralLengthMultiplication(fillnum + 2);
    			  }
    		  }
    	  }
      }
    }
  }
  
  public class WordIterator implements Iterator
  {
    public int startIndex;
    private int wordsWalked;
    private int currWord;
    private int nextWord;
    private int currRow;
    private int flcount;
    private int []word;
    private int fillnum1;
    private int [] fillnum2;

    private volatile boolean hasNextWord = false;

    WordIterator()
    {
      startIndex = -1;
      wordsWalked = 0;
      currRow = -1;
      flcount = 0;
    }

    public void advanceTo(int endCount)			//閿熸枻鎷�
    {
      while (hasNext() && wordsWalked < endCount) {
         next();
      }
      if (wordsWalked <= endCount) {
        return;
      }
      if(is1_fill(currWord))
      nextWord = SEQUENCE_1_FILL | (wordsWalked - endCount);   //閿熸枻鎷烽敓锟�
      else
      {
    	  if(is0_fill(currWord))
    		  nextWord = wordsWalked - endCount;
    	  else
    	  {
    		  if(isF1_L_F2(currWord))
    		  {
		          //filltype[1] = (currWord & 0x04000000) >>> 26;
        	      if(flcount == 1)
    			  {
    				  
    					  nextWord = wordsWalked - endCount;
    				  
    				  
    			  }
    			  else
    			  {
    				  if(flcount == 0)
    				  {
        				   nextWord = wordsWalked - endCount;
    				  }
    			  }
    			  /*int[] fillnum = getFLFFILLWords(currWord);
    			  if(endCount >= startIndex && endCount< startIndex + fillnum[0])
    			  {
    				  if(filltype[0] == 0)
    				  {
    					  nextWord = wordsWalked - endCount - 1;
    				  }
    				  else
    				  {
    					  nextWord = SEQUENCE_1_FILL | (wordsWalked - endCount - 1);
    				  }
    			  }
    			  else
    			  {
    				  if(endCount >= startIndex + fillnum[0] + 1 && endCount < startIndex +fillnum[0] + fillnum[1] + 1)
    				  {
    					  if(filltype[1] == 0)
        				  {
        					  nextWord = wordsWalked - endCount - 1;
        				  }
        				  else
        				  {
        					  nextWord = SEQUENCE_1_FILL | (wordsWalked - endCount - 1);
        				  }
    				  }
    			  }*/
    		  }
    		  else
    		  {
    			  if(is0L1_F_0L2(currWord))
    			  {
    				  //int filltype = (currWord & 0x00800000) >>> 23;
    					  nextWord = wordsWalked - endCount;
    			  }
    				  
    		  }
    	  }
      }
      startIndex = endCount;
      hasNextWord = true;
    }

    @Override
    public boolean hasNext()
    {
      if(flcount != 0)
    	  return true;
      if (isEmpty()) {
        return false;
      }
      if (hasNextWord) {
        return true;
      }
      return currRow < (words.capacity() - 1);
    }

    @Override
    public Integer next()
    {
      if (hasNextWord) {
    	 // if(is0L1_F_0L2(currWord) || is0L_F_1L(currWord)
		//		  || is1L1_F_1L2(currWord) || is1L_F_0L(currWord)
		//		  || isF1_L_F2(currWord))
    	  if(is0L1_F_0L2(currWord) || isF1_L_F2(currWord))
    	  {
    		  hasNextWord = false;
    		  return new Integer(nextWord);
    	  }
    	  else
    	  {
    		currWord = nextWord;
    		hasNextWord = false;
    	  }
        return new Integer(currWord);
      }
      if(flcount == 0)
      {
    	  currWord = words.get(++currRow);
      }
      if (isLiteral(currWord)) {
        startIndex = wordsWalked++;
      } else {
    	if(is0_fill(currWord) || is1_fill(currWord))
        {
    		startIndex = wordsWalked;
    		wordsWalked += getSequenceCount(currWord);
        }
    	else
    	{
 //   		if(is0L1_F_0L2(currWord) || is0L_F_1L(currWord)
   // 				|| is1L_F_0L(currWord) || is1L1_F_1L2(currWord))
    		if(is0L1_F_0L2(currWord))
    		{
    			int []literalbyte;
    			int []literalPos;
    			if(flcount == 0)
    			{
    				word = new int[3];
    			//filltype = (currWord & 0x00800000) >>> 23;
      			//fillnum = (currWord & 0x007f0000) >>> 16;
        	    fillnum1 = (currWord & 0x00ff0000) >>> 16;
        	    literalbyte = getLFLLiteralWords(currWord);
        	    literalPos = new int [2];
        	    //literalPos[0] = (currWord & 0x0c000000) >>> 26;
        	    //literalPos[1] = (currWord & 0x03000000) >>> 24;
        	    literalPos[0] = (currWord & 0x18000000) >>> 27;
        	    literalPos[1] = (currWord & 0x06000000) >>> 25;
        	    
        	    	word[0] = 0x80000000 | (literalbyte[0] <<(literalPos[0] * 8));
        	    	word[2] = 0x80000000 | (literalbyte[1] <<(literalPos[1] * 8));
        	    	word[1] = (0x00000000) | fillnum1;
        	    /*else
        	    {
        	    	if(is1L1_F_1L2(currWord))
            	    {
        	    		words[0] = ((literalbyte[0]) << (literalPos[0] * 8));
	  		    		  switch(literalPos[0])
	  		    		  {
	  		    		  case 0: words[0] |= 0xffffff00; break;
	  		    		  case 1: words[0] |= 0xffff00ff; break;
	  		    		  case 2: words[0] |= 0xff00ffff; break;
	  		    		  default: words[0] |= 0x80ffffff;
	  		    		  }
	  		    		  
	  		    		words[2] = ((literalbyte[1]) << (literalPos[1] * 8));
	  		    		  switch(literalPos[1])
	  		    		  {
	  		    		  case 0: words[2] |= 0xffffff00; break;
	  		    		  case 1: words[2] |= 0xffff00ff; break;
	  		    		  case 2: words[2] |= 0xff00ffff; break;
	  		    		  default: words[2] |= 0x80ffffff;
	  		    		  }
            	    }
        	    	else
        	    	{
        	    		if(is1L_F_0L(currWord))
                	    {
        	    			words[0] = ((literalbyte[0]) << (literalPos[0] * 8));
	      		    		  switch(literalPos[0])
	      		    		  {
	      		    		  case 0: words[0] |= 0xffffff00; break;
	      		    		  case 1: words[0] |= 0xffff00ff; break;
	      		    		  case 2: words[0] |= 0xff00ffff; break;
	      		    		  default: words[0] |= 0x80ffffff;
	      		    		  }
            	    		words[2] = 0x80000000 | (literalbyte[1] <<(literalPos[1] * 8));
                	    }
        	    		else
        	    		{
        	    			if(is0L_F_1L(currWord))
                    	    {
                	    		words[0] = 0x80000000 | (literalbyte[0] <<(literalPos[0] * 8));
                	    		words[2] = ((literalbyte[1]) << (literalPos[1] * 8));
	          		    		  switch(literalPos[1])
	          		    		  {
	          		    		  case 0: words[2] |= 0xffffff00; break;
	          		    		  case 1: words[2] |= 0xffff00ff; break;
	          		    		  case 2: words[2] |= 0xff00ffff; break;
	          		    		  default: words[2] |= 0x80ffffff;
	          		    		  }
                    	    }
        	    		}
        	    	}
        	    }*/
	        	    
	        	    	//words[1] = (filltype << 28) | fillnum;
	        	    	
	        	    
    			}
        	    if(flcount == 0)
        	    {
        	    	startIndex = wordsWalked;
        	    	wordsWalked ++;
        	    	flcount ++;
        	    	return new Integer(word[0]);
        	    }
        	    else
        	    {
        	    	if(flcount == 1)
        	    	{
        	    		startIndex = wordsWalked;
        	    		wordsWalked += fillnum1;
        	    		flcount ++;
        	    		return new Integer(word[1]);
        	    	}
        	    	else
        	    	{
        	    		if(flcount == 2)
        	    		{
        	    			startIndex = wordsWalked;
        	    			wordsWalked ++;
        	    			flcount = 0;
        	    			return new Integer(word[2]);
        	    		}
        	    	}
        	    }
    		}
    		else
    		{
    			if(isF1_L_F2(currWord))
    		      {
    		    	  
    		    	 /* else
    		    	  {
    		    		  words[1] = ((literalbyte) << (literalPos * 8));
    		    		  switch(literalPos)
    		    		  {
    		    		  case 0: words[1] |= 0xffffff00; break;
    		    		  case 1: words[1] |= 0xffff00ff; break;
    		    		  case 2: words[1] |= 0xff00ffff; break;
    		    		  default: words[1] |= 0x80ffffff;
    		    		  }
    		    		   
    		    	  }*/
    		    	  if(flcount == 0)
    		    	  {
    		    		  word = new int[3];
        		    	  fillnum2 = new int[2];
        		    	  //int literaltype = (currWord & 0x08000000) >>> 27;
        		          int literalbyte = (currWord & 0x0000ff00) >>> 8;
        		          //int literalPos = (currWord & 0x03000000) >>> 24;
        		          int literalPos = (currWord & 0x06000000) >>> 25;
        		          //filltype[1] = (currWord & 0x04000000) >>> 26;
        		          fillnum2[0] = (currWord & 0x00ff0000) >>> 16;
        		          fillnum2[1] = (currWord & 0x000000ff);
    		        	    
    		        	    	word[0] = (0x00000000) | fillnum2[0];
    		        	    
    	  	        	    
    		        	    
    		        	    	word[2] = (0x00000000) | fillnum2[1];
    		        	    
        		    	 // words[0] = (filltype[0] << 28) | fillnum[0];
        		    	  //words[2] = (filltype[1] << 28) | fillnum[1];
        		    	  
        		    		  word[1] = 0x80000000 | (literalbyte <<(literalPos * 8));
        		    	  
    		    		  startIndex = wordsWalked;
    		    		  wordsWalked += fillnum2[0];
    		    		  flcount ++;
    		    		  return new Integer(word[0]);
    		    	  }
    		    	  else
    		    	  {
    		    		  if(flcount == 1)
    		    		  {
    		    			  startIndex = wordsWalked ++;
    		    			  flcount ++;
    		    			  return new Integer(word[1]);
    		    		  }
    		    		  else
    		    		  {
    		    			  if(flcount == 2)
    		    			  {
    		    				  startIndex = wordsWalked;
    		    				  wordsWalked += fillnum2[1];
    		    				  flcount = 0;
    		    				  return new Integer(word[2]);
    		    			  }
    		    		  }
    		    	  }
    		      }
    		}
    			
    	}
      }

      return new Integer(currWord);
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  private static class WordHolder
  {
    private final int word;
    private final WordIterator iterator;

    public WordHolder(
        int word,
        WordIterator iterator
    )
    {
      this.word = word;
      this.iterator = iterator;
    }

    public int getWord()
    {
      return word;
    }

    public WordIterator getIterator()
    {
      return iterator;
    }
  }

}
