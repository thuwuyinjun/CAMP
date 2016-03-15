/*
* Copyright 2012 Metamarkets Group Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package it.uniroma3.mat.extendedset.intset;


import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;
import it.uniroma3.mat.extendedset.utilities.IntList;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ImmutableConciseSet
{
  private final static int CHUNK_SIZE = 10000;

  public static ImmutableConciseSet newImmutableFromMutable(ConciseSet conciseSet)
  {
    if (conciseSet == null || conciseSet.isEmpty()) {
      return new ImmutableConciseSet();
    }
    int[] words = conciseSet.getWords();
    IntList retVal = new IntList();
    for(int i = 0; i<words.length; i++)
    {
    	if(ConciseSetUtils.isOneSequence(words[i]))
    	{
    		words[i] &= 0x0fffffff;
    		words[i] |= 0x10000000;
    		words[i] += 1;
    	}
    	else
    	{
    		if(ConciseSetUtils.isZeroSequence(words[i]))
    		{
    			words[i] &= 0x0fffffff;
    			words[i] += 1;
    		}
    		else
    		{
    			if(ConciseSetUtils.isAllOnesLiteral(words[i]))
    			{
    				words[i] = 0x10000001;
    			}
    			else
    			{
    				if(ConciseSetUtils.isAllZerosLiteral(words[i]))
    				{
    					words[i] = 1;
    				}
    			}
    		}
    	}
    	addAndCompact(retVal, words[i]);
    }
    //return new ImmutableConciseSet(IntBuffer.wrap(words));
    IntBuffer buffer =  IntBuffer.wrap(retVal.toArray());
    return new ImmutableConciseSet(buffer);
  }

  public static int compareInts(int x, int y)
  {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public static ImmutableConciseSet union(ImmutableConciseSet... sets)
  {
    return union(Arrays.asList(sets));
  }

  public static ImmutableConciseSet union(Iterable<ImmutableConciseSet> sets)
  {
    return union(sets.iterator());
  }

  public static ImmutableConciseSet union(Iterator<ImmutableConciseSet> sets)
  {
    ImmutableConciseSet partialResults = doUnion(Iterators.limit(sets, CHUNK_SIZE));
    while (sets.hasNext()) {
      final UnmodifiableIterator<ImmutableConciseSet> partialIter = Iterators.singletonIterator(partialResults);
      partialResults = doUnion(Iterators.<ImmutableConciseSet>concat(partialIter, Iterators.limit(sets, CHUNK_SIZE)));
    }
    return partialResults;
  }

  public static ImmutableConciseSet intersection(ImmutableConciseSet... sets)
  {
    return intersection(Arrays.asList(sets));
  }

  public static ImmutableConciseSet intersection(Iterable<ImmutableConciseSet> sets)
  {
    return intersection(sets.iterator());
  }

  public static ImmutableConciseSet intersection(Iterator<ImmutableConciseSet> sets)
  {
    ImmutableConciseSet partialResults = doIntersection(Iterators.limit(sets, CHUNK_SIZE));
    while (sets.hasNext()) {
      final UnmodifiableIterator<ImmutableConciseSet> partialIter = Iterators.singletonIterator(partialResults);
      partialResults = doIntersection(
          Iterators.<ImmutableConciseSet>concat(Iterators.limit(sets, CHUNK_SIZE), partialIter)
      );
    }
    return partialResults;
  }

  public static ImmutableConciseSet complement(ImmutableConciseSet set)
  {
    return doComplement(set);
  }

  public static ImmutableConciseSet complement(ImmutableConciseSet set, int length)
  {
    if (length <= 0) {
      return new ImmutableConciseSet();
    }

    // special case when the set is empty and we need a concise set of ones
    if (set == null || set.isEmpty()) {
      ConciseSet newSet = new ConciseSet();
      for (int i = 0; i < length; i++) {
        newSet.add(i);
      }
      return ImmutableConciseSet.newImmutableFromMutable(newSet);
    }

    IntList retVal = new IntList();
    int endIndex = length - 1;

    int wordsWalked = 0;
    int last = 0;

    WordIterator iter = set.newWordIterator();

    while (iter.hasNext()) {
        int word = iter.next();
        wordsWalked = iter.wordsWalked;
        if (ConciseSetUtils.isLiteral(word)) {
          retVal.add(ConciseSetUtils.ALL_ZEROS_LITERAL | ~word);
        } else {
      	  if(ConciseSetUtils.is0_fill(word))
      	  {
      		  retVal.add(0x10000000 | ConciseSetUtils.getSequenceCount(word));
      	  }
      	  else
      	  {
      		  retVal.add(ConciseSetUtils.getSequenceCount(word));
      	  }
          //retVal.add(ConciseSetUtils.SEQUENCE_BIT ^ word);
        }
      }

    last = set.getLast();

    int distFromLastWordBoundary = ConciseSetUtils.maxLiteralLengthModulus(last);
    int distToNextWordBoundary = ConciseSetUtils.MAX_LITERAL_LENGTH - distFromLastWordBoundary - 1;
    last = (last < 0) ? 0 : last + distToNextWordBoundary;

    int diff = endIndex - last;
    // only append a new literal when the end index is beyond the current word
    if (diff > 0) {
      // first check if the difference can be represented in 31 bits
        // create a fill from last set bit to endIndex for number of 31 bit blocks minus one
    	if (diff <= ConciseSetUtils.MAX_LITERAL_LENGTH) {
            retVal.add(ConciseSetUtils.ALL_ONES_LITERAL);
          } else {
            // create a fill from last set bit to endIndex for number of 31 bit blocks minus one
            int endIndexWordCount = ConciseSetUtils.maxLiteralLengthDivision(endIndex);
            retVal.add(0x10000000 | (endIndexWordCount - wordsWalked));
            retVal.add(ConciseSetUtils.ALL_ONES_LITERAL);
          }
    	}

    // clear bits after last set value
    int lastWord = retVal.get(retVal.length() - 1);
    if (ConciseSetUtils.isLiteral(lastWord)) {
        lastWord = ConciseSetUtils.clearBitsAfterInLastWord(
            lastWord,
            ConciseSetUtils.maxLiteralLengthModulus(endIndex)
        );
      }

    retVal.set(retVal.length() - 1, lastWord);
    trimZeros(retVal);

    if (retVal.isEmpty()) {
      return new ImmutableConciseSet();
    }
    return compact(new ImmutableConciseSet(IntBuffer.wrap(retVal.toArray())));
  }

  public static ImmutableConciseSet compact(ImmutableConciseSet set)
  {
    IntList retVal = new IntList();
    WordIterator itr = set.newWordIterator();
    while (itr.hasNext()) {
      addAndCompact(retVal, itr.next());
    }
    IntBuffer buffer =  IntBuffer.wrap(retVal.toArray());
    return new ImmutableConciseSet(buffer);
  }
  
  public static int ConvertFill(int word)
  {
	  if(ConciseSetUtils.isOneSequence(word))
	  {
		  word &= 0x0fffffff;
		  word |= 0x10000000;
		  word += 1;
	  }
	  else
	  {
		  if(ConciseSetUtils.isZeroSequence(word))
		  {
			  word &= 0x0fffffff;
			  word += 1;
		  }
		  else
		  {
			  if(ConciseSetUtils.isAllOnesLiteral(word))
			  {
				  word = 0x10000001;
			  }
			  else
			  {
				  if(ConciseSetUtils.isAllZerosLiteral(word))
				  {
					  word = 0x00000001;
				  }
			  }
		  }
	  }
	  return word;
  }
  private static void addAndCompact(IntList set, int wordToAdd)		//改
  {
    int length = set.length();
    //wordToAdd = ConvertFill(wordToAdd);
    if (set.isEmpty()) {
      set.add(wordToAdd);
      return;
    }
    
    int last = set.get(length - 1);

    int newWord = 0;
    if(ConciseSetUtils.is0_fill(wordToAdd) || ConciseSetUtils.is1_fill(wordToAdd))
    {					//2 0-fll compressed
    	int fillkind = ConciseSetUtils.is0_fill(wordToAdd) ? 0 : 1;
    	int fillcount = ConciseSetUtils.getSequenceCount(wordToAdd);
    	if(ConciseSetUtils.is0_fill(last) || ConciseSetUtils.is1_fill(last))
    	{
    		int filltype = ConciseSetUtils.is0_fill(last) ? 0 : 1;
    		if(filltype == fillkind)
    		{
    			if(ConciseSetUtils.getSequenceCount(wordToAdd) + ConciseSetUtils.getSequenceCount(last) <= ConciseSetUtils.MAX_FILL_NUM)
    			{
    				newWord = ConciseSetUtils.getSequenceCount(wordToAdd) + ConciseSetUtils.getSequenceCount(last);
    				if(filltype == 1)
    					newWord |= 0x10000000;
    				set.set(length - 1, newWord);
    				return;
    			}
    		}
    	}
    	else
    	{				//0-fill is compressed into a f-l-f
    		if(ConciseSetUtils.isF1_L_F2(last))
    		{
    			int[] fillnum = ConciseSetUtils.getFLFFILLWords(last);
    			int filltype = (last & 0x04000000) >>> 26;
      			if(filltype == fillkind)
      			{
      				if(fillnum[1] + ConciseSetUtils.getSequenceCount(wordToAdd) <= ConciseSetUtils.MAX_F_L_F_NUM)
      				{
      					newWord = last + ConciseSetUtils.getSequenceCount(wordToAdd);
      					set.set(length - 1, newWord);
      					return;
      				}
      			}
    		}
    		else
    		{
    			if(length > 1)
    			{
    				int pre_last = set.get(length - 2);
    				if(ConciseSetUtils.is0_fill(pre_last) || ConciseSetUtils.is1_fill(pre_last))
    				{
    					int filltype = ConciseSetUtils.is0_fill(pre_last) ? 0 : 1;
    					int fillnum = ConciseSetUtils.getSequenceCount(pre_last);
    					if(ConciseSetUtils.isDirtyByte0Word(last) || ConciseSetUtils.isDirtyByte1Word(last))
    					{
    						int literaltype = ConciseSetUtils.isDirtyByte0Word(last) ? 0 : 1;
    						int literalPos = ConciseSetUtils.isDirtyByte0Word(last) ? ConciseSetUtils.getDirtyByte0Pos(last) : ConciseSetUtils.getDirtyByte1Pos(last);
    						int literalByte = ConciseSetUtils.isDirtyByte0Word(last) ? ConciseSetUtils.getDirty0(last) : ConciseSetUtils.getDirty1(last);
    						if(fillnum <= ConciseSetUtils.MAX_F_L_F_NUM && fillcount <= ConciseSetUtils.MAX_F_L_F_NUM)
    						{
    							newWord = ConciseSetUtils.SEQUENCE_F1_L_F2 | fillcount | fillnum << 16 | literalByte << 8
    							| literaltype << 27 | literalPos << 24 | fillkind << 26 | filltype << 28;
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
    	if(length > 1)
    	{
    		int pre_last = set.get(length - 2);
    		if(ConciseSetUtils.isDirtyByte0Word(wordToAdd) || ConciseSetUtils.isDirtyByte1Word(wordToAdd))
    		{
    			int literaltype2 = ConciseSetUtils.isDirtyByte0Word(wordToAdd) ? 0 : 1;
    			int literalPos2 = ConciseSetUtils.isDirtyByte0Word(wordToAdd) ? ConciseSetUtils.getDirtyByte0Pos(wordToAdd) : ConciseSetUtils.getDirtyByte1Pos(wordToAdd);
    			int literalByte2 = ConciseSetUtils.isDirtyByte0Word(wordToAdd) ? ConciseSetUtils.getDirty0(wordToAdd) : ConciseSetUtils.getDirty1(wordToAdd);
    			if(ConciseSetUtils.is0_fill(last) || ConciseSetUtils.is1_fill(last))
    			{
    				int filltype = ConciseSetUtils.is0_fill(last) ? 0 : 1;
    				int fillnum = ConciseSetUtils.getSequenceCount(last);
    				if(fillnum <= ConciseSetUtils.MAX_L_F_L_NUM)
    				{
    					if(ConciseSetUtils.isDirtyByte0Word(pre_last) || ConciseSetUtils.isDirtyByte1Word(pre_last))
    					{
    						int literaltype1 = ConciseSetUtils.isDirtyByte0Word(pre_last) ? 0 : 1;
    		    			int literalPos1 = ConciseSetUtils.isDirtyByte0Word(pre_last) ? ConciseSetUtils.getDirtyByte0Pos(pre_last) : ConciseSetUtils.getDirtyByte1Pos(pre_last);
    		    			int literalByte1 = ConciseSetUtils.isDirtyByte0Word(pre_last) ? ConciseSetUtils.getDirty0(pre_last) : ConciseSetUtils.getDirty1(pre_last);
    		    			if(literaltype1 == 0 && literaltype2 == 0)
    		    			{
    		    				newWord = ConciseSetUtils.SEQUENCE_0L1_F_0L2 | literalByte1 << 8 | literalByte2 | fillnum << 16
    		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
    		    				set.set(length - 2, newWord);
        						set.setLength(length - 2);
        						return;	
    		    			}
    		    			else
    		    			{
    		    				if(literaltype1 == 1 && literaltype2 == 0)
        		    			{
        		    				newWord = ConciseSetUtils.SEQUENCE_1L_F_0L | literalByte1 << 8 | literalByte2 | fillnum << 16
        		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
        		    				set.set(length - 2, newWord);
            						set.setLength(length - 2);
            						return;	
        		    			}
    		    				else
    		    				{
    		    					if(literaltype1 == 0 && literaltype2 == 1)
    	    		    			{
    	    		    				newWord = ConciseSetUtils.SEQUENCE_0L_F_1L | literalByte1 << 8 | literalByte2 | fillnum << 16
    	    		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
    	    		    				set.set(length - 2, newWord);
    	        						set.setLength(length - 2);
    	        						return;	
    	    		    			}
    		    					else
    		    					{
    		    						if(literaltype1 == 1 && literaltype2 == 1)
    		    		    			{
    		    		    				newWord = ConciseSetUtils.SEQUENCE_1L1_F_1L2 | literalByte1 << 8 | literalByte2 | fillnum << 16
    		    		    					| filltype << 23 | literalPos1 << 26 | literalPos2 << 24;
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
    		}
    	}
    }
    /*if (ConciseSetUtils.isAllOnesLiteral(last)) {
      if (ConciseSetUtils.isAllOnesLiteral(wordToAdd)) {
        newWord = 0x10000001;
      } else if (ConciseSetUtils.isOneSequence(wordToAdd) && ConciseSetUtils.getFlippedBit(wordToAdd) == -1) {
        newWord = wordToAdd + 1;
      }
    } else if (ConciseSetUtils.isOneSequence(last)) {
      if (ConciseSetUtils.isAllOnesLiteral(wordToAdd)) {
        newWord = last + 1;
      } else if (ConciseSetUtils.isOneSequence(wordToAdd) && ConciseSetUtils.getFlippedBit(wordToAdd) == -1) {
        newWord = last + ConciseSetUtils.getSequenceNumWords(wordToAdd);
      }
    } else if (ConciseSetUtils.isAllZerosLiteral(last)) {
      if (ConciseSetUtils.isAllZerosLiteral(wordToAdd)) {
        newWord = 0x00000001;
      } else if (ConciseSetUtils.isZeroSequence(wordToAdd) && ConciseSetUtils.getFlippedBit(wordToAdd) == -1) {
        newWord = wordToAdd + 1;
      }
    } else if (ConciseSetUtils.isZeroSequence(last)) {
      if (ConciseSetUtils.isAllZerosLiteral(wordToAdd)) {
        newWord = last + 1;
      } else if (ConciseSetUtils.isZeroSequence(wordToAdd) && ConciseSetUtils.getFlippedBit(wordToAdd) == -1) {
        newWord = last + ConciseSetUtils.getSequenceNumWords(wordToAdd);
      }
    } else if (ConciseSetUtils.isLiteralWithSingleOneBit(last)) {
      int position = Integer.numberOfTrailingZeros(last) + 1;
      if (ConciseSetUtils.isAllZerosLiteral(wordToAdd)) {
        newWord = 0x00000001 | (position << 25);
      } else if (ConciseSetUtils.isZeroSequence(wordToAdd) && ConciseSetUtils.getFlippedBit(wordToAdd) == -1) {
        newWord = (wordToAdd + 1) | (position << 25);
      }
    } else if (ConciseSetUtils.isLiteralWithSingleZeroBit(last)) {
      int position = Integer.numberOfTrailingZeros(~last) + 1;
      if (ConciseSetUtils.isAllOnesLiteral(wordToAdd)) {
        newWord = 0x40000001 | (position << 25);
      } else if (ConciseSetUtils.isOneSequence(wordToAdd) && ConciseSetUtils.getFlippedBit(wordToAdd) == -1) {
        newWord = (wordToAdd + 1) | (position << 25);
      }
    }*/

    
      set.add(wordToAdd);
  }

  private static ImmutableConciseSet doUnion(Iterator<ImmutableConciseSet> sets)
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

            if (ConciseSetUtils.is1_fill(w1)) {
              if (ConciseSetUtils.is1_fill(w2)) {
                return -compareInts(ConciseSetUtils.getSequenceNumWords(w1), ConciseSetUtils.getSequenceNumWords(w2));
              }
              return -1;
            } else if (ConciseSetUtils.isLiteral(w1)) {
              if (ConciseSetUtils.is1_fill(w2)) {
                return 1;
              } else if (ConciseSetUtils.isLiteral(w2)) {
                return 0;
              }
              return -1;
            } else {
              if (!ConciseSetUtils.is0_fill(w2)) {
                return 1;
              }
              return compareInts(ConciseSetUtils.getSequenceNumWords(w1), ConciseSetUtils.getSequenceNumWords(w2));
            }
          }
        }
    ).create();

    // populate priority queue
    while (sets.hasNext()) {
      ImmutableConciseSet set = sets.next();

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

      if (ConciseSetUtils.is1_fill(word)) {
        // extract a literal from the flip bits of the one sequence
        //int flipBitLiteral = ConciseSetUtils.getLiteralFromOneSeqFlipBit(word);

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
            if (ConciseSetUtils.isOneSequence(w)) {
              flipBitLiteral |= ConciseSetUtils.getLiteralFromOneSeqFlipBit(w);
            } else if (ConciseSetUtils.isLiteral(w)) {
              flipBitLiteral |= w;
            } else {
              flipBitLiteral |= ConciseSetUtils.getLiteralFromZeroSeqFlipBit(w);
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
        if (flipBitLiteral != ConciseSetUtils.ALL_ONES_LITERAL) {
          flipBitLiteral ^= ConciseSetUtils.ALL_ONES_LITERAL;
          int position = Integer.numberOfTrailingZeros(flipBitLiteral) + 1;
          newWord |= (position << 25);
        }*/
        addAndCompact(retVal, word);
        currIndex = itr.wordsWalked;

        if (itr.hasNext()) {
          wordsToAdd.add(new WordHolder(itr.next(), itr));
        }
      } else if (ConciseSetUtils.isLiteral(word)) {
        // advance all other literals
        WordHolder nextVal = theQ.peek();
        while (nextVal != null &&
               nextVal.getIterator().startIndex == itr.startIndex) {

          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();

          // if we still have zero fills with flipped bits, OR them here
          if (ConciseSetUtils.isLiteral(w)) {
            word |= w;
            if(word == 0xffffffff)
            	word = 0x10000001;
          } else {
            /*int flipBitLiteral = ConciseSetUtils.getLiteralFromZeroSeqFlipBit(w);
            if (flipBitLiteral != ConciseSetUtils.ALL_ZEROS_LITERAL) {
              word |= flipBitLiteral;
              i.advanceTo(itr.wordsWalked);
            }*/
        	if(ConciseSetUtils.is1_fill(w))
        	{
        		word = 0x10000001;
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
      return new ImmutableConciseSet();
    }
    return new ImmutableConciseSet(IntBuffer.wrap(retVal.toArray()));
  }

  public static ImmutableConciseSet doIntersection(Iterator<ImmutableConciseSet> sets)
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

            if (ConciseSetUtils.is0_fill(w1)) {
              if (ConciseSetUtils.is0_fill(w2)) {
                return -compareInts(ConciseSetUtils.getSequenceNumWords(w1), ConciseSetUtils.getSequenceNumWords(w2));
              }
              return -1;
            } else if (ConciseSetUtils.isLiteral(w1)) {
              if (ConciseSetUtils.is0_fill(w2)) {
                return 1;
              } else if (ConciseSetUtils.isLiteral(w2)) {
                return 0;
              }
              return -1;
            } else {
              if (!ConciseSetUtils.is1_fill(w2)) {
                return 1;
              }
              return compareInts(ConciseSetUtils.getSequenceNumWords(w1), ConciseSetUtils.getSequenceNumWords(w2));
            }
          }
        }
    ).create();

    // populate priority queue
    while (sets.hasNext()) {
      ImmutableConciseSet set = sets.next();

      if (set == null || set.isEmpty()) {
        return new ImmutableConciseSet();
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
        addAndCompact(retVal, (0x10000000 | (itr.startIndex - currIndex)));
        currIndex = itr.startIndex;
      }

      if (ConciseSetUtils.is0_fill(word)) {
        // extract a literal from the flip bits of the zero sequence
        //int flipBitLiteral = ConciseSetUtils.getLiteralFromZeroSeqFlipBit(word);

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
            if (ConciseSetUtils.isZeroSequence(w)) {
              flipBitLiteral &= ConciseSetUtils.getLiteralFromZeroSeqFlipBit(w);
            } else if (ConciseSetUtils.isLiteral(w)) {
              flipBitLiteral &= w;
            } else {
              flipBitLiteral &= ConciseSetUtils.getLiteralFromOneSeqFlipBit(w);
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
        /*if (flipBitLiteral != ConciseSetUtils.ALL_ZEROS_LITERAL) {
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
      } else if (ConciseSetUtils.isLiteral(word)) {
        // advance all other literals
        WordHolder nextVal = theQ.peek();
        while (nextVal != null &&
               nextVal.getIterator().startIndex == itr.startIndex) {

          WordHolder entry = theQ.poll();
          int w = entry.getWord();
          WordIterator i = entry.getIterator();

          // if we still have one fills with flipped bits, AND them here
          if (ConciseSetUtils.isLiteral(w)) {
            word &= w;
          } else {
            /*int flipBitLiteral = ConciseSetUtils.getLiteralFromOneSeqFlipBit(w);
            if (flipBitLiteral != ConciseSetUtils.ALL_ONES_LITERAL) {
              word &= flipBitLiteral;
              i.advanceTo(itr.wordsWalked);
            }*/
        	if(ConciseSetUtils.is0_fill(w))
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
          /*flipBitLiteral = ConciseSetUtils.getLiteralFromOneSeqFlipBit(w);
          if (flipBitLiteral != ConciseSetUtils.ALL_ONES_LITERAL) {
            wordsToAdd.add(new WordHolder(flipBitLiteral, i));
          } else */if (i.hasNext()) {
            wordsToAdd.add(new WordHolder(i.next(), i));
          } else {
            wordsWalkedAtSequenceEnd = Math.min(i.wordsWalked, wordsWalkedAtSequenceEnd);
          }

          nextVal = theQ.peek();
        }

        // check if a literal needs to be created from the flipped bits of this sequence
        //flipBitLiteral = ConciseSetUtils.getLiteralFromOneSeqFlipBit(word);
        /*if (flipBitLiteral != ConciseSetUtils.ALL_ONES_LITERAL) {
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
      addAndCompact(retVal, (0x10000000 | (wordsWalkedAtSequenceEnd - currIndex)));
    }

    if (retVal.isEmpty()) {
      return new ImmutableConciseSet();
    }
    return new ImmutableConciseSet(IntBuffer.wrap(retVal.toArray()));
  }

  public static ImmutableConciseSet doComplement(ImmutableConciseSet set)
  {
    if (set == null || set.isEmpty()) {
      return new ImmutableConciseSet();
    }

    IntList retVal = new IntList();
    WordIterator iter = set.newWordIterator();
    while (iter.hasNext()) {
      int word = iter.next();
      if (ConciseSetUtils.isLiteral(word)) {
        retVal.add(ConciseSetUtils.ALL_ZEROS_LITERAL | ~word);
      } else {
    	  if(ConciseSetUtils.is0_fill(word))
    	  {
    		  retVal.add(0x10000000 | ConciseSetUtils.getSequenceCount(word));
    	  }
    	  else
    	  {
    		  retVal.add(ConciseSetUtils.getSequenceCount(word));
    	  }
        //retVal.add(ConciseSetUtils.SEQUENCE_BIT ^ word);
      }
    }
    // do not complement after the last element
    int lastWord = retVal.get(retVal.length() - 1);
    if (ConciseSetUtils.isLiteral(lastWord)) {
      lastWord = ConciseSetUtils.clearBitsAfterInLastWord(
          lastWord,
          ConciseSetUtils.maxLiteralLengthModulus(set.getLast())
      );
    }

    retVal.set(retVal.length() - 1, lastWord);

    trimZeros(retVal);

    if (retVal.isEmpty()) {
      return new ImmutableConciseSet();
    }
    return new ImmutableConciseSet(IntBuffer.wrap(retVal.toArray()));
  }

  // Based on the ConciseSet implementation by Alessandro Colantonio
  private static void trimZeros(IntList set)
  {
    // loop over ALL_ZEROS_LITERAL words
    int w;
    int last = set.length() - 1;
    do {
      w = set.get(last);
      if(ConciseSetUtils.is0_fill(w))
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

  public ImmutableConciseSet()
  {
    this.words = null;
    this.lastWordIndex = -1;
    this.size = 0;
  }
  
  public int[] getWords()
  {
	  return words.array();
  }
  public ImmutableConciseSet(ByteBuffer byteBuffer)
  {
    this.words = byteBuffer.asIntBuffer();
    this.lastWordIndex = words.capacity() - 1;
    this.size = calcSize();
  }

  public ImmutableConciseSet(IntBuffer buffer)
  {
    this.words = buffer;
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

  // Based on the ConciseSet implementation by Alessandro Colantonio
  private int calcSize()  //改
  {
    int retVal = 0;
    for (int i = 0; i <= lastWordIndex; i++) {
      int w = words.get(i);
      if (ConciseSetUtils.isLiteral(w)) {
        retVal += ConciseSetUtils.getLiteralBitCount(w);
      } else {
        if (ConciseSetUtils.is0_fill(w)) {
            retVal += 0;
        } else {
          if(ConciseSetUtils.is1_fill(w))
        	  retVal += ConciseSetUtils.maxLiteralLengthMultiplication(ConciseSetUtils.getSequenceCount(w));
          else
          {
        	  if(ConciseSetUtils.isF1_L_F2(w))
        	  {
        		  int[] fillnum = ConciseSetUtils.getFLFFILLWords(w);
        		  int[] filltype = new int [2];
        		  int literalByte = ConciseSetUtils.getFLFLiteralWords(w);
        		  int literaltype = (w & 0x08000000) >>> 27;
    			  int num;
    			  if(literaltype == 0)
    			  {
    				  num = ConciseSetUtils.getLiteralBitCount(literalByte);
    			  }
    			  else
    			  {
    				  num = ConciseSetUtils.getLiteralBitCount(literalByte) + 23;
    			  }
        		  filltype[0] = (w & 0x10000000) >>> 28;
    			  filltype[1] = (w & 0x04000000) >>> 26;
        		  retVal += ConciseSetUtils.maxLiteralLengthMultiplication(filltype[0] * fillnum[0] + filltype[1] * fillnum[1])
        		  	+ num;
        	  }
        	  else
        	  {
        		  if(ConciseSetUtils.is0L1_F_0L2(w))
        		  {
        			  int fillnum = ConciseSetUtils.getLFLFILLWords(w);
        			  int filltype = (w & 0x00800000) >>> 23;
        			  int[] literalByte = ConciseSetUtils.getLFLLiteralWords(w);
        			  int[]literalnum = new int [2];
        			  literalnum[0] = ConciseSetUtils.getLiteralBitCount(literalByte[0]);
        			  literalnum[1] = ConciseSetUtils.getLiteralBitCount(literalByte[1]);
        			  retVal += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
        			       + literalnum[1];
        		  }
        		  else
        		  {
        			  if(ConciseSetUtils.is1L1_F_1L2(w))
            		  {
            			  int fillnum = ConciseSetUtils.getLFLFILLWords(w);
            			  int filltype = (w & 0x00800000) >>> 23;
            			  int[] literalByte = ConciseSetUtils.getLFLLiteralWords(w);
            			  int[]literalnum = new int [2];
            			  literalnum[0] = ConciseSetUtils.getLiteralBitCount(literalByte[0]) + 23;
            			  literalnum[1] = ConciseSetUtils.getLiteralBitCount(literalByte[1]) + 23;
            			  retVal += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
            			       + literalnum[1];
            		  }
        			  else
        			  {
        				  if(ConciseSetUtils.is0L_F_1L(w))
                		  {
                			  int fillnum = ConciseSetUtils.getLFLFILLWords(w);
                			  int filltype = (w & 0x00800000) >>> 23;
                			  int[] literalByte = ConciseSetUtils.getLFLLiteralWords(w);
                			  int[]literalnum = new int [2];
                			  literalnum[0] = ConciseSetUtils.getLiteralBitCount(literalByte[0]);
                			  literalnum[1] = ConciseSetUtils.getLiteralBitCount(literalByte[1]) + 23;
                			  retVal += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
                			       + literalnum[1];
                		  }
        				  else
        				  {
        					  if(ConciseSetUtils.is1L_F_0L(w))
        	        		  {
        	        			  int fillnum = ConciseSetUtils.getLFLFILLWords(w);
        	        			  int filltype = (w & 0x00800000) >>> 23;
        	        			  int[] literalByte = ConciseSetUtils.getLFLLiteralWords(w);
        	        			  int[]literalnum = new int [2];
        	        			  literalnum[0] = ConciseSetUtils.getLiteralBitCount(literalByte[0]) + 23;
        	        			  literalnum[1] = ConciseSetUtils.getLiteralBitCount(literalByte[1]);
        	        			  retVal += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum * filltype) + literalnum[0]
        	        			       + literalnum[1];
        	        		  }
        				  }
        			  }
        		  }
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
      if (ConciseSetUtils.isLiteral(w)) {
        last += ConciseSetUtils.MAX_LITERAL_LENGTH;
      } else {
    	  if(ConciseSetUtils.is0_fill(w) || ConciseSetUtils.is1_fill(w))
    		  last += ConciseSetUtils.maxLiteralLengthMultiplication(ConciseSetUtils.getSequenceCount(w));
    	  else
    	  {
    		  if(ConciseSetUtils.isF1_L_F2(w))
    		  {
    			  int[] fillnum = ConciseSetUtils.getFLFFILLWords(w);
    			  last += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum[0] + fillnum[1] + 1);
    		  }
    		  else
    		  {
    			  if(ConciseSetUtils.is0L1_F_0L2(w) || ConciseSetUtils.is0L_F_1L(w) 
    					  || ConciseSetUtils.is1L1_F_1L2(w) || ConciseSetUtils.is1L_F_0L(w))
    			  {
    				  int fillnum = ConciseSetUtils.getLFLFILLWords(w);
    				  last += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum + 2);
    			  }
    				  
    		  }
    	  }
      }
    }

    int w = words.get(lastWordIndex);
    if (ConciseSetUtils.isLiteral(w)) {
      last -= Integer.numberOfLeadingZeros(ConciseSetUtils.getLiteralBits(w));
    } else {
    	if(ConciseSetUtils.is1_fill(w))
    		last--;
    	else
    	{
    		if(ConciseSetUtils.is0L1_F_0L2(w) || ConciseSetUtils.is0L_F_1L(w) 
    				|| ConciseSetUtils.is1L1_F_1L2(w) || ConciseSetUtils.is1L_F_0L(w))
    		{
    			int[] fillByte = ConciseSetUtils.getLFLLiteralWords(w);
    			last -= Integer.numberOfLeadingZeros(ConciseSetUtils.getLiteralBits(fillByte[1]));
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
      if (ConciseSetUtils.isLiteral(w)) {
        // number of bits in the current word
        setBitsInCurrentWord = ConciseSetUtils.getLiteralBitCount(w);

        // check if the desired bit is in the current word
        if (position < setBitsInCurrentWord) {
          int currSetBitInWord = -1;
          for (; position >= 0; position--) {
            currSetBitInWord = Integer.numberOfTrailingZeros(w & (0xFFFFFFFF << (currSetBitInWord + 1)));
          }
          return firstSetBitInWord + currSetBitInWord;
        }

        // skip the 31-bit block
        firstSetBitInWord += ConciseSetUtils.MAX_LITERAL_LENGTH;
      } else {
        // number of involved bits (31 * blocks)
        setBitsInCurrentWord = 0;
        // check the sequence type
        if (ConciseSetUtils.is1_fill(w) || ConciseSetUtils.is0_fill(w)) {
        	int sequenceLength = ConciseSetUtils.maxLiteralLengthMultiplication(ConciseSetUtils.getSequenceCount(w));
            if(ConciseSetUtils.is1_fill(w))
            	setBitsInCurrentWord = sequenceLength;
            if (position < setBitsInCurrentWord) 
            {
              return firstSetBitInWord + position;
            }
            firstSetBitInWord += sequenceLength;
        } else {
          if(ConciseSetUtils.isF1_L_F2(w))
          {
        	  int[]fillnum = ConciseSetUtils.getFLFFILLWords(w);
        	  int[]fillType = new int[2];
        	  int literalType = (w & 0x08000000) >>> 27;
              int literalByte = ConciseSetUtils.getFLFLiteralWords(w);
              int literalPos = (w & 0x03000000) >>> 24;
        	  fillType[0] = (w & 0x10000000) >>> 28;
              fillType[1] = (w & 0x04000000) >>> 26;
    	      setBitsInCurrentWord = fillType[0] * fillnum[0];
    	      
    	      if(position < setBitsInCurrentWord)
    	      {
    	    	  return firstSetBitInWord + position;
    	      }
    	      firstSetBitInWord += setBitsInCurrentWord;
    	      position -= setBitsInCurrentWord;
    	      int literal;
    	      if(literalType == 0)
    	      {
    	    	  literal = 0x80000000 | (literalByte << (literalPos * 8));
    	      }
    	      else
    	      {
    	    	  literal = 0x80000000 | (literalByte << (literalPos * 8));
    	    	  switch(literalPos)
    	    	  {
    	    	  case 0: literal |= 0xffffff00;break;
    	    	  case 1: literal |= 0xffff00ff;break;
    	    	  case 2: literal |= 0xff00ffff;break;
    	    	  case 3: literal |= 0x80ffffff;break;
    	    	  }
    	      }
    	      setBitsInCurrentWord = ConciseSetUtils.getLiteralBitCount(literal);
    	      if (position < setBitsInCurrentWord) {
    	          int currSetBitInWord = -1;
    	          for (; position >= 0; position--) {
    	            currSetBitInWord = Integer.numberOfTrailingZeros(literal & (0xFFFFFFFF << (currSetBitInWord + 1)));
    	          }
    	          return firstSetBitInWord + currSetBitInWord;
    	       }
    	      firstSetBitInWord += ConciseSetUtils.MAX_LITERAL_LENGTH;
    	      position -= setBitsInCurrentWord;
    	      
    	      setBitsInCurrentWord = fillType[1] * fillnum[1];
    	      if(position < setBitsInCurrentWord)
    	      {
    	    	  return firstSetBitInWord + position;
    	      }
    	      firstSetBitInWord += setBitsInCurrentWord;
          }
          else
          {
        	  if(ConciseSetUtils.is0L1_F_0L2(w) || ConciseSetUtils.is1L1_F_1L2(w)
        			  || ConciseSetUtils.is0L_F_1L(w) || ConciseSetUtils.is1L_F_0L(w))
        	  {
        		  int fillnum = ConciseSetUtils.getLFLFILLWords(w);
        		  int filltype = (w & 0x00800000) >>> 23;
    	          int[] literalType = new int [2];
    	          int[] literalByte = ConciseSetUtils.getLFLLiteralWords(w);
    	          int[] literalPos = new int [2];
    	          literalPos[0] = (w & 0x0c000000) >>> 26;
    	          literalPos[1] = (w & 0x03000000) >>> 24;
    	    	  int[] literal = new int [2];
    	    	  if(ConciseSetUtils.is0L1_F_0L2(w))
    	    	  {
    	    		  literalType[0] = 0;
    	    		  literalType[1] = 0;
    	    	  }
    	    	  else
    	    	  {
    	    		  if(ConciseSetUtils.is1L1_F_1L2(w))
        	    	  {
        	    		  literalType[0] = 1;
        	    		  literalType[1] = 1;
        	    	  }
    	    		  else
    	    		  {
    	    			  if(ConciseSetUtils.is0L_F_1L(w))
    	    	    	  {
    	    	    		  literalType[0] = 0;
    	    	    		  literalType[1] = 1;
    	    	    	  }
    	    			  else
    	    			  {
    	    				  if(ConciseSetUtils.is1L_F_0L(w))
    	        	    	  {
    	        	    		  literalType[0] = 1;
    	        	    		  literalType[1] = 0;
    	        	    	  }
    	    			  }
    	    		  }
    	    	  }
    	          if(literalType[0] == 0)
        	      {
        	    	  literal[0] = 0x80000000 | (literalByte[0] << (literalPos[0] * 8));
        	      }
        	      else
        	      {
        	    	  literal[0] = 0x80000000 | (literalByte[0] << (literalPos[0] * 8));
        	    	  switch(literalPos[0])
        	    	  {
        	    	  case 0: literal[0] |= 0xffffff00;break;
        	    	  case 1: literal[0] |= 0xffff00ff;break;
        	    	  case 2: literal[0] |= 0xff00ffff;break;
        	    	  case 3: literal[0] |= 0x80ffffff;break;
        	    	  }
        	      }
    	          if(literalType[1] == 0)
        	      {
        	    	  literal[1] = 0x80000000 | (literalByte[1] << (literalPos[1] * 8));
        	      }
        	      else
        	      {
        	    	  literal[1] = 0x80000000 | (literalByte[1] << (literalPos[1] * 8));
        	    	  switch(literalPos[1])
        	    	  {
        	    	  case 0: literal[1] |= 0xffffff00;break;
        	    	  case 1: literal[1] |= 0xffff00ff;break;
        	    	  case 2: literal[1] |= 0xff00ffff;break;
        	    	  case 3: literal[1] |= 0x80ffffff;break;
        	    	  }
        	      }
    	          
    	          setBitsInCurrentWord = ConciseSetUtils.getLiteralBitCount(literal[0]);
        	      if (position < setBitsInCurrentWord) {
        	          int currSetBitInWord = -1;
        	          for (; position >= 0; position--) {
        	            currSetBitInWord = Integer.numberOfTrailingZeros(literal[0] & (0xFFFFFFFF << (currSetBitInWord + 1)));
        	          }
        	          return firstSetBitInWord + currSetBitInWord;
        	       }
        	      firstSetBitInWord += ConciseSetUtils.MAX_LITERAL_LENGTH;
        	      position -= setBitsInCurrentWord;
        	      
        	      setBitsInCurrentWord = fillnum * filltype;
        	      if(position < setBitsInCurrentWord)
        	      {
        	    	  return firstSetBitInWord + position;
        	      }
        	      firstSetBitInWord += setBitsInCurrentWord;
        	      position -= setBitsInCurrentWord;
        	      
        	      setBitsInCurrentWord = ConciseSetUtils.getLiteralBitCount(literal[1]);
        	      if (position < setBitsInCurrentWord) {
        	          int currSetBitInWord = -1;
        	          for (; position >= 0; position--) {
        	            currSetBitInWord = Integer.numberOfTrailingZeros(literal[1] & (0xFFFFFFFF << (currSetBitInWord + 1)));
        	          }
        	          return firstSetBitInWord + currSetBitInWord;
        	       }
        	      firstSetBitInWord += ConciseSetUtils.MAX_LITERAL_LENGTH;
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

  public int compareTo(ImmutableConciseSet other)
  {
    return words.asReadOnlyBuffer().compareTo(other.words.asReadOnlyBuffer());
  }

  private boolean isEmpty()
  {
    return words == null;
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
    
    //改
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
    final ConciseSetUtils.LiteralAndZeroFillExpander litExp;
    final ConciseSetUtils.OneFillExpander oneExp;

    ConciseSetUtils.WordExpander exp;
    int nextIndex = 0;
    int nextOffset = 0;

    private BitIterator()
    {
      litExp = ConciseSetUtils.newLiteralAndZeroFillExpander();
      oneExp = ConciseSetUtils.newOneFillExpander();

      nextWord();
    }

    private BitIterator(
        ConciseSetUtils.LiteralAndZeroFillExpander litExp,
        ConciseSetUtils.OneFillExpander oneExp,
        ConciseSetUtils.WordExpander exp,
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
          (ConciseSetUtils.LiteralAndZeroFillExpander) litExp.clone(),
          (ConciseSetUtils.OneFillExpander) oneExp.clone(),
          exp.clone(),
          nextIndex,
          nextOffset
      );
    }

    private void nextWord()
    {
      final int word = words.get(nextIndex++);
      exp = ConciseSetUtils.is1_fill(word) ? oneExp : litExp;
      exp.reset(nextOffset, word, true);

      // prepare next offset
      if (ConciseSetUtils.isLiteral(word)) {
        nextOffset += ConciseSetUtils.MAX_LITERAL_LENGTH;
      } else {
        nextOffset += ConciseSetUtils.maxLiteralLengthMultiplication(ConciseSetUtils.getSequenceCount(word));
      }
    }
  }
  */
  // Based on the ConciseSet implementation by Alessandro Colantonio
  private class BitIterator implements IntSet.IntIterator
  {
    final ConciseSetUtils.LiteralAndZeroFillExpander litExp;
    final ConciseSetUtils.OneFillExpander oneExp;
    final ConciseSetUtils.FLFExpander flfExp;
    final ConciseSetUtils.LFLExpander lflExp;

    ConciseSetUtils.WordExpander exp;
    int nextIndex = 0;
    int nextOffset = 0;

    private BitIterator()
    {
      litExp = ConciseSetUtils.newLiteralAndZeroFillExpander();
      oneExp = ConciseSetUtils.newOneFillExpander();
      flfExp = ConciseSetUtils.newFLFExpander();
      lflExp = ConciseSetUtils.newLFLExpander();
      nextWord();
    }

    private BitIterator(
        ConciseSetUtils.LiteralAndZeroFillExpander litExp,
        ConciseSetUtils.OneFillExpander oneExp,
        ConciseSetUtils.WordExpander exp,
        ConciseSetUtils.FLFExpander flfExp,
        ConciseSetUtils.LFLExpander lflExp,
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
          (ConciseSetUtils.LiteralAndZeroFillExpander) litExp.clone(),
          (ConciseSetUtils.OneFillExpander) oneExp.clone(),
          exp.clone(),
          (ConciseSetUtils.FLFExpander) flfExp.clone(),
          (ConciseSetUtils.LFLExpander) lflExp.clone(),
          nextIndex,
          nextOffset
      );
    }

    private void nextWord()
    {
      final int word = words.get(nextIndex++);
      if(ConciseSetUtils.is1_fill(word))		//改
    	  exp = oneExp;
      else
      {
    	  if(ConciseSetUtils.is0_fill(word) || ConciseSetUtils.isLiteral(word))
    	  {
    		  exp = litExp;
    	  }
    	  else
    	  {
    		  if(ConciseSetUtils.isF1_L_F2(word))
    		  {
    			  exp = flfExp;
    		  }
    		  else
    		  {
    			  if(ConciseSetUtils.is0L1_F_0L2(word) || ConciseSetUtils.is0L_F_1L(word) 
    					  || ConciseSetUtils.is1L1_F_1L2(word) || ConciseSetUtils.is1L_F_0L(word))
    			  {
    				  exp = lflExp;
    			  }
    		  }
    	  }
      }
      exp.reset(nextOffset, word, true);

      // prepare next offset
      if (ConciseSetUtils.isLiteral(word)) {
        nextOffset += ConciseSetUtils.MAX_LITERAL_LENGTH;
      } else {
    	  if(ConciseSetUtils.is0_fill(word) || ConciseSetUtils.is1_fill(word))
    		  nextOffset += ConciseSetUtils.maxLiteralLengthMultiplication(ConciseSetUtils.getSequenceCount(word));
    	  else
    	  {
    		  if(ConciseSetUtils.isF1_L_F2(word))
    		  {
    			  int[] fillnum = ConciseSetUtils.getFLFFILLWords(word);
    			  nextOffset += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum[0] + fillnum[1] + 1);
    		  }
    		  else
    		  {
    			  if(ConciseSetUtils.is1L1_F_1L2(word) || ConciseSetUtils.is1L_F_0L(word) 
    					  || ConciseSetUtils.is0L1_F_0L2(word) || ConciseSetUtils.is0L_F_1L(word))
    			  {
    				  int fillnum = ConciseSetUtils.getLFLFILLWords(word);
    				  nextOffset += ConciseSetUtils.maxLiteralLengthMultiplication(fillnum + 2);
    			  }
    		  }
    	  }
      }
    }
  }
  
  public class WordIterator implements Iterator
  {
    private int startIndex;
    private int wordsWalked;
    private int currWord;
    private int nextWord;
    private int currRow;
    private int flcount;

    private volatile boolean hasNextWord = false;

    WordIterator()
    {
      startIndex = -1;
      wordsWalked = 0;
      currRow = -1;
      flcount = 0;
    }

    public void advanceTo(int endCount)			//改
    {
      while (hasNext() && wordsWalked < endCount) {
         next();
      }
      if (wordsWalked <= endCount) {
        return;
      }
      if(ConciseSetUtils.is1_fill(currWord))
      nextWord = 0x10000000 | (wordsWalked - endCount);   //需改
      else
      {
    	  if(ConciseSetUtils.is0_fill(currWord))
    		  nextWord = wordsWalked - endCount;
    	  else
    	  {
    		  if(ConciseSetUtils.isF1_L_F2(currWord))
    		  {
    			  int []filltype = new int [2];
    			  filltype[0] = (currWord & 0x10000000) >>> 28;
		          filltype[1] = (currWord & 0x04000000) >>> 26;
        	      if(flcount == 1)
    			  {
    				  if(filltype[0] == 0)
    				  {
    					  nextWord = wordsWalked - endCount;
    				  }
    				  else
    				  {
    					  nextWord = 0x10000000 | (wordsWalked - endCount);
    				  }
    			  }
    			  else
    			  {
    				  if(flcount == 0)
    				  {
    					  if(filltype[1] == 0)
        				  {
        					  nextWord = wordsWalked - endCount;
        				  }
        				  else
        				  {
        					  nextWord = 0x10000000 | (wordsWalked - endCount);
        				  }
    				  }
    			  }
    			  /*int[] fillnum = ConciseSetUtils.getFLFFILLWords(currWord);
    			  if(endCount >= startIndex && endCount< startIndex + fillnum[0])
    			  {
    				  if(filltype[0] == 0)
    				  {
    					  nextWord = wordsWalked - endCount - 1;
    				  }
    				  else
    				  {
    					  nextWord = 0x10000000 | (wordsWalked - endCount - 1);
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
        					  nextWord = 0x10000000 | (wordsWalked - endCount - 1);
        				  }
    				  }
    			  }*/
    		  }
    		  else
    		  {
    			  if(ConciseSetUtils.is0L1_F_0L2(currWord) || ConciseSetUtils.is0L_F_1L(currWord)
    					  || ConciseSetUtils.is1L1_F_1L2(currWord) || ConciseSetUtils.is1L_F_0L(currWord))
    			  {
    				  int filltype = (currWord & 0x00800000) >>> 23;
        	    	  if(filltype == 0)
    				  {
    					  nextWord = wordsWalked - endCount;
    				  }
    				  else
    				  {
    					  nextWord = 0x10000000 | (wordsWalked - endCount);
    				  }
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
    	  if(ConciseSetUtils.is0L1_F_0L2(currWord) || ConciseSetUtils.is0L_F_1L(currWord)
				  || ConciseSetUtils.is1L1_F_1L2(currWord) || ConciseSetUtils.is1L_F_0L(currWord)
				  || ConciseSetUtils.isF1_L_F2(currWord))
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
      if (ConciseSetUtils.isLiteral(currWord)) {
        startIndex = wordsWalked++;
      } else {
    	if(ConciseSetUtils.is0_fill(currWord) || ConciseSetUtils.is1_fill(currWord))
        {
    		startIndex = wordsWalked;
    		wordsWalked += ConciseSetUtils.getSequenceCount(currWord);
        }
    	else
    	{
    		if(ConciseSetUtils.is0L1_F_0L2(currWord) || ConciseSetUtils.is0L_F_1L(currWord)
    				|| ConciseSetUtils.is1L_F_0L(currWord) || ConciseSetUtils.is1L1_F_1L2(currWord))
    		{
    			int []words = new int [3];
    			int filltype = (currWord & 0x00800000) >>> 23;
      			int fillnum = (currWord & 0x007f0000) >>> 16;
        	    int []literalbyte = ConciseSetUtils.getLFLLiteralWords(currWord);
        	    int []literalPos = new int [2];
        	    literalPos[0] = (currWord & 0x0c000000) >>> 26;
        	    literalPos[1] = (currWord & 0x03000000) >>> 24;
        	    if(ConciseSetUtils.is0L1_F_0L2(currWord))
        	    {
        	    	words[0] = 0x80000000 | (literalbyte[0] <<(literalPos[0] * 8));
        	    	words[2] = 0x80000000 | (literalbyte[1] <<(literalPos[1] * 8));
        	    }
        	    else
        	    {
        	    	if(ConciseSetUtils.is1L1_F_1L2(currWord))
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
        	    		if(ConciseSetUtils.is1L_F_0L(currWord))
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
        	    			if(ConciseSetUtils.is0L_F_1L(currWord))
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
        	    }
        	    words[1] = (filltype << 28) | fillnum;
        	    if(flcount == 0)
        	    {
        	    	startIndex = wordsWalked;
        	    	wordsWalked ++;
        	    	flcount ++;
        	    	return new Integer(words[0]);
        	    }
        	    else
        	    {
        	    	if(flcount == 1)
        	    	{
        	    		startIndex = wordsWalked;
        	    		wordsWalked += fillnum;
        	    		flcount ++;
        	    		return new Integer(words[1]);
        	    	}
        	    	else
        	    	{
        	    		if(flcount == 2)
        	    		{
        	    			startIndex = wordsWalked;
        	    			wordsWalked ++;
        	    			flcount = 0;
        	    			return new Integer(words[2]);
        	    		}
        	    	}
        	    }
    		}
    		else
    		{
    			if(ConciseSetUtils.isF1_L_F2(currWord))
    		      {
    		    	  int []words = new int [3];
    		    	  int []filltype = new int [2];
    		    	  int []fillnum = new int[2];
    		    	  int literaltype = (currWord & 0x08000000) >>> 27;
    		          int literalbyte = (currWord & 0x0000ff00) >>> 8;
    		          int literalPos = (currWord & 0x03000000) >>> 24;
    		    	  filltype[0] = (currWord & 0x10000000) >>> 28;
    		          filltype[1] = (currWord & 0x04000000) >>> 26;
    		          fillnum[0] = (currWord & 0x00ff0000) >>> 16;
    		          fillnum[1] = (currWord & 0x000000ff);
    		    	  words[0] = (filltype[0] << 28) | fillnum[0];
    		    	  words[2] = (filltype[1] << 28) | fillnum[1];
    		    	  if(literaltype == 0)
    		    	  {
    		    		  words[1] = 0x80000000 | (literalbyte <<(literalPos * 8));
    		    	  }
    		    	  else
    		    	  {
    		    		  words[1] = ((literalbyte) << (literalPos * 8));
    		    		  switch(literalPos)
    		    		  {
    		    		  case 0: words[1] |= 0xffffff00; break;
    		    		  case 1: words[1] |= 0xffff00ff; break;
    		    		  case 2: words[1] |= 0xff00ffff; break;
    		    		  default: words[1] |= 0x80ffffff;
    		    		  }
    		    		   
    		    	  }
    		    	  if(flcount == 0)
    		    	  {
    		    		  startIndex = wordsWalked;
    		    		  wordsWalked += fillnum[0];
    		    		  flcount ++;
    		    		  return new Integer(words[0]);
    		    	  }
    		    	  else
    		    	  {
    		    		  if(flcount == 1)
    		    		  {
    		    			  startIndex = wordsWalked ++;
    		    			  flcount ++;
    		    			  return new Integer(words[1]);
    		    		  }
    		    		  else
    		    		  {
    		    			  if(flcount == 2)
    		    			  {
    		    				  startIndex = wordsWalked;
    		    				  wordsWalked += fillnum[1];
    		    				  flcount = 0;
    		    				  return new Integer(words[2]);
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
