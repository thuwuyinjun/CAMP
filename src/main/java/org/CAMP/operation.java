package org.CAMP;
import java.util.ArrayList;
public class operation {
	public static index intersection(index id1, index id2)
	{
		
		BitSet[]set1 = id1.set;
		BitSet []set2 = id2.set;
		int [] count1 = id1.count;
		int [] count2 = id2.count;
		
		int result_size = id1.size + id2.size;// ? id1.size : id2.size;
              //  int result_size = ? id1.size : id2.size;
		BitSet [] set = new BitSet[result_size * 2];
		int [] count = new int[result_size];
		
		result_size = 0;
		int i1 = 0;
		int i2 = 0;
		int max1 = id1.size;
		int max2 = id2.size;
		int i;
		boolean pre = false;
		BitSet tp1_alpha = new BitSet();
		BitSet tp2_alpha = new BitSet();
		BitSet tp1_beta = new BitSet();
		BitSet tp2_beta = new BitSet();
		
		tp1_alpha = set1[i1 * 2];
		tp2_alpha = set2[i2 * 2];
		tp1_beta = set1[i1 * 2 + 1];
		tp2_beta = set2[i2 * 2 + 1];
		int alpha_num1 = count1[i1];
		int alpha_num2 = count2[i2];
		
		BitSet prefix_tp = new BitSet();
		BitSet alpha_tp = new BitSet();
		BitSet beta_tp = new BitSet();
		BitSet tmp;
		int prefix_num = 0;
		int num_tp;
		
		while(true)
		{
			if(alpha_num1 == alpha_num2)
			{
//				alpha_tp.clear();
//				beta_tp = new BitSet();
				alpha_tp = tp1_alpha;
				alpha_tp.and(tp2_alpha);
				beta_tp = tp1_beta;
				
					beta_tp.and(tp2_beta);
					if(beta_tp.isEmpty())
					{
						if(pre == false)
						{
							prefix_tp = alpha_tp;
							prefix_num = alpha_num1 + 1;
							pre = true;
						}
						else
						{
							num_tp = prefix_num;
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num1 + 1;
							pre = true;
						}
						
					}
					else
					{
						if(pre == false)
						{
							set[ 2 *result_size] = alpha_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = alpha_num1;
							result_size ++;
						}
						else
						{						
							num_tp = prefix_num;
							
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num1;
							
							
							alpha_tp = prefix_tp;
//							prefix_tp.clear();
							pre = false;
							set[ 2 *result_size] = prefix_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;
						}
					}
				
				i1 += 1;
				i2 += 1;
				if(i1 < max1 && i2 < max2)
				{
					tp1_alpha = set1[2 * i1];
					tp2_alpha = set2[2 * i2];
					tp1_beta = set1[2 * i1 + 1];
					tp2_beta = set2[2 * i2 + 1];
					alpha_num1 = count1[i1];
					alpha_num2 = count2[i2];
				}
				else
				{
					if(pre == true)
					{
						set[ 2 *result_size] = prefix_tp;
						beta_tp = new BitSet();
						set[ 2 *result_size + 1] = beta_tp;
						count[result_size] = prefix_num;
						result_size ++;
					}
					break;
				}
			}
			else
			{
				if(alpha_num1 < alpha_num2)
				{
//					beta_tp.clear();
					tmp = tp2_alpha;
					alpha_tp = tmp.get(0, alpha_num1);
					alpha_tp.and(tp1_alpha);
										
//					if( !tmp.get(alpha_num1,alpha_num1+1).isEmpty())
//						beta_tp = tp1_beta;
					
					if(tmp.get(alpha_num1,alpha_num1+1).isEmpty())
					{
						if(pre == false)
						{
							prefix_tp = alpha_tp;
							prefix_num = alpha_num1 + 1;
							pre = true;
						}
						else
						{
							num_tp = prefix_num;
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num1 + 1;
							pre = true;
						}
						
					}
					else
					{
						if(pre == false)
						{
							set[ 2 *result_size] = alpha_tp;
							set[ 2 *result_size + 1] = tp1_beta;
							count[result_size] = alpha_num1;
							result_size ++;
						}
						else
						{						
							num_tp = prefix_num;
							
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num1;
							alpha_tp = prefix_tp;
//							prefix_tp.clear();
							pre = false;
							set[ 2 *result_size] = prefix_tp;
							set[ 2 *result_size + 1] = tp1_beta;
							count[result_size] = prefix_num;
							result_size ++;
						}
					}
					alpha_tp = tmp.get(alpha_num1+1, alpha_num2);
					tp2_alpha = alpha_tp;
					alpha_num2 = alpha_num2 - alpha_num1 - 1;

					i1 += 1;
					if(i1< max1)
					{
						tp1_alpha = set1[2 * i1];
						tp1_beta = set1[2 * i1 +1];
						alpha_num1 = count1[i1];
					}
					else
					{
						if(pre == true)
						{
							set[ 2 *result_size] = prefix_tp;
							beta_tp = new BitSet();
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;
						}
						break;
					}
					

				}
				else
				{
//					beta_tp.clear();
					tmp = tp1_alpha;
					alpha_tp = tmp.get(0, alpha_num2);
					alpha_tp.and(tp2_alpha);					
//					if(!tmp.get(alpha_num2,alpha_num2+1).isEmpty())
//						beta_tp = tp2_beta;

					
					if(tmp.get(alpha_num2,alpha_num2+1).isEmpty())
					{
						if(pre == false)
						{
							prefix_tp = alpha_tp;
							prefix_num = alpha_num2 + 1;
							pre = true;
						}
						else
						{
							num_tp = prefix_num;
							for(i = 0; i<alpha_num2 ;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num2 + 1;
							pre = true;
						}
						
					}
					else
					{
						if(pre == false)
						{
							set[ 2 *result_size] = alpha_tp;
							set[ 2 *result_size + 1] = tp2_beta;
							count[result_size] = alpha_num2;
							result_size ++;
						}
						else
						{						
							num_tp = prefix_num;
							
							for(i = 0; i<alpha_num2;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num2;
							alpha_tp = prefix_tp;
//							prefix_tp.clear();
							pre = false;
							set[ 2 *result_size] = prefix_tp;
							set[ 2 *result_size + 1] = tp2_beta;
							count[result_size] = prefix_num;
							result_size ++;
							
							}
					}
					
					
					alpha_tp = tmp.get(alpha_num2+1, alpha_num1);
					
					tp1_alpha = alpha_tp;
					alpha_num1 = alpha_num1 - alpha_num2 - 1;
					
					i2 += 1;
					if(i2< max2)
					{
						tp2_alpha = set2[2 * i2];
						tp2_beta = set2[2 * i2 +1];
						alpha_num2 = count2[i2];
					}
					else
					{
						if(pre == true)
						{
							set[ 2 *result_size] = prefix_tp;
							beta_tp = new BitSet();
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;
						}
						break;
					}
				}
			}
		}
		
		index id = new index();
		id.count = count;
		id.set = set;
		id.size = result_size;
		id.num = id1.num;
		return id;
	}

	public static index union(index id1, index id2)
	{
		
		BitSet[]set1 = id1.set;
		BitSet []set2 = id2.set;
		int [] count1 = id1.count;
		int [] count2 = id2.count;
		
		int result_size = id1.size + id2.size;
		BitSet [] set = new BitSet[result_size * 2];
		int [] count = new int[result_size];
		
		result_size = 0;
		int i1 = 0;
		int i2 = 0;
		int max1 = id1.size;
		int max2 = id2.size;
		int i;
		boolean pre = false;
		BitSet tp1_alpha = new BitSet();
		BitSet tp2_alpha = new BitSet();
		BitSet tp1_beta = new BitSet();
		BitSet tp2_beta = new BitSet();
		
		tp1_alpha = set1[i1 * 2];
		tp2_alpha = set2[i2 * 2];
		tp1_beta = set1[i1 * 2 + 1];
		tp2_beta = set2[i2 * 2 + 1];
		int alpha_num1 = count1[i1];
		int alpha_num2 = count2[i2];
		
		BitSet prefix_tp = new BitSet();
		BitSet alpha_tp = new BitSet();
		BitSet beta_tp = new BitSet();
		BitSet tmp;
		int prefix_num = 0;
		int num_tp;
		
		while(true)
		{
			if(alpha_num1 == alpha_num2)
			{
				beta_tp = new BitSet();
				alpha_tp = tp1_alpha;
				alpha_tp.or(tp2_alpha);
				beta_tp = tp1_beta;
				
					beta_tp.or(tp2_beta);
					if(beta_tp.nextClearBit(0) >= id1.num)
					{
						if(pre == false)
						{
							prefix_tp = alpha_tp;
							prefix_tp.set(alpha_num1);
							prefix_num = alpha_num1 + 1;
							pre = true;
						}
						else
						{
							num_tp = prefix_num;
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_tp.set(num_tp + alpha_num1);
							prefix_num = prefix_num + alpha_num1 + 1;
							pre = true;
						}
						
					}
					else
					{
						if(pre == false)
						{
							set[ 2 *result_size] = alpha_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = alpha_num1;
							result_size ++;
						}
						else
						{						
							num_tp = prefix_num;
							
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num1;
							alpha_tp = prefix_tp;
							pre = false;
							set[ 2 *result_size] = prefix_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;
						}
					}
				
				i1 += 1;
				i2 += 1;
				if(i1 < max1 && i2 < max2)
				{
					tp1_alpha = set1[2 * i1];
					tp2_alpha = set2[2 * i2];
					tp1_beta = set1[2 * i1 + 1];
					tp2_beta = set2[2 * i2 + 1];
					alpha_num1 = count1[i1];
					alpha_num2 = count2[i2];
				}
				else
				{
					if(pre == true)
					{
						set[ 2 *result_size] = prefix_tp;
						beta_tp = new BitSet();
						set[ 2 *result_size + 1] = beta_tp;
						count[result_size] = prefix_num;
						result_size ++;
					}
					break;
				}
			}
			else
			{
				if(alpha_num1 < alpha_num2)
				{
					tmp = tp2_alpha;
					alpha_tp = tmp.get(0, alpha_num1);
					alpha_tp.or(tp1_alpha);
										
//					if( !tmp.get(alpha_num1,alpha_num1+1).isEmpty())
//						beta_tp = tp1_beta;
					
					if( !tmp.get(alpha_num1,alpha_num1+1).isEmpty())
					{
						if(pre == false)
						{
							prefix_tp = alpha_tp;
							prefix_tp.set(alpha_num1);
							prefix_num = alpha_num1 + 1;
							pre = true;
						}
						else
						{
							num_tp = prefix_num;
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_tp.set(num_tp+alpha_num1);
							prefix_num = prefix_num + alpha_num1 + 1;
							pre = true;
						}
						
					}
					else
					{
						beta_tp = tp1_beta;
						if(pre == false)
						{
							set[ 2 *result_size] = alpha_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = alpha_num1;
							result_size ++;
						}
						else
						{						
							num_tp = prefix_num;
							
							for(i = 0; i<alpha_num1;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num1;
							alpha_tp = prefix_tp;
							pre = false;
							set[ 2 *result_size] = prefix_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;
						}
					}
					alpha_tp = tmp.get(alpha_num1+1, alpha_num2);
					
					tp2_alpha = alpha_tp;
					alpha_num2 = alpha_num2 - alpha_num1 - 1;
					i1 += 1;
					if(i1< max1)
					{
						tp1_alpha = set1[2 * i1];
						tp1_beta = set1[2 * i1 +1];
						alpha_num1 = count1[i1];
					}
					else
					{
						if(pre == true)
						{
							
							set[ 2 *result_size] = prefix_tp;
							beta_tp = new BitSet();
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;
						}
						break;
					}
					

				}
				else
				{
					tmp = tp1_alpha;
					alpha_tp = tmp.get(0, alpha_num2);
					alpha_tp.or(tp2_alpha);					
//					if(!tmp.get(alpha_num2,alpha_num2+1).isEmpty())
//						beta_tp = tp2_beta;

					
					if(!tmp.get(alpha_num2,alpha_num2+1).isEmpty())
					{
						if(pre == false)
						{
							prefix_tp = alpha_tp;
							prefix_tp.set(alpha_num2,true);
							prefix_num = alpha_num2 + 1;
							pre = true;
						}
						else
						{
							num_tp = prefix_num;
							for(i = 0; i<alpha_num2 ;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_tp.set(num_tp+alpha_num2);
							prefix_num = prefix_num + alpha_num2 + 1;
							pre = true;
						}
						
					}
					else
					{
						beta_tp = tp2_beta;
						if(pre == false)
						{
							set[ 2 *result_size] = alpha_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = alpha_num2;
							result_size ++;				
						}
						else
						{						
							num_tp = prefix_num;
							
							for(i = 0; i<alpha_num2;i++)
							{
								prefix_tp.set(num_tp+i, alpha_tp.get(i));
							}
							prefix_num = prefix_num + alpha_num2;

							alpha_tp = prefix_tp;
							pre = false;
							set[ 2 *result_size] = prefix_tp;
							set[ 2 *result_size + 1] = beta_tp;
							count[result_size] = prefix_num;
							result_size ++;				
							}
					}
					
					
					alpha_tp = tmp.get(alpha_num2+1, alpha_num1);
					
					tp1_alpha = alpha_tp;
					alpha_num1 = alpha_num1 - alpha_num2 - 1;

					i2 += 1;
					if(i2< max2)
					{
						tp2_alpha = set2[i2 * 2];
						tp2_beta = set2[2 * i2 + 1];
						alpha_num2 = count2[i2];
					}
					else
					{
						if(pre == true)
						{
							set[result_size * 2] = prefix_tp;
							count[result_size] = prefix_num;
							beta_tp = new BitSet();
							set[result_size * 2 + 1] = beta_tp;
						}
						break;
					}
				}
			}
		}
		index id = new index();
		id.set = set;
		id.count = count;
		id.size = result_size;
		id.num = id1.num;
		return id;
	}
//	public static  index Complement(index id)
//	{
//		combination[] set = id.com;
//		combination[] st = new combination[id.size];
//		combination com = new combination ();
//		int result_size = 0;
//		for(int i = 0 ; i<id.size;i++)
//		{
//			com = new combination();
//			com = set[i];
//			if(set[i].alphanum!=0)
//			com.alpha.flip(0, set[i].alphanum);
//			if(id.num!=0)
//				com.beta.flip(0, id.num);
//			st[result_size ++] = com;
//		}
//		index result = new index();
//		result.com = st;
//		result.size = result_size;
//		result.num = id.num;
//		return id;
//	}
}
