package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 18.
 *
 */
public class DevysNounFilterTest {
	
	List<String> nounsList = new ArrayList<String>();
	StringReader reader = new StringReader("�ڹ����α׷��� �������� ����ϰ� ���� �ý����� �ϵ� ���� �ý����� ����Ͽ� �ϵӿ� ���� �˾ƺ��ϴ�.");
	
	@Before
	public void setUp() throws Exception {
		nounsList.add("�ڹ�");
		nounsList.add("�ڹ����α׷���");
		nounsList.add("������");
		nounsList.add("��������");
		nounsList.add("����ϰ�");
		nounsList.add("����");
		nounsList.add("�ý�����");
		nounsList.add("�ý���");
		nounsList.add("�ϵ�");
		nounsList.add("�ý�����");
		nounsList.add("����Ͽ�");
		nounsList.add("�ϵӿ�");
		nounsList.add("����");
		nounsList.add("�˾ƺ��ϴ�");
		nounsList.add(".");
	}
	
	@Test
	public void testTokenStream() throws IOException {
		TokenStream stream = new DevysNounFilter(new DevysTokenizer(reader));
		CharTermAttribute charTermAtt = stream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = stream.getAttribute(OffsetAttribute.class);
		TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionAtt = stream.getAttribute(PositionIncrementAttribute.class);
		
		List<String> extractedNouns = new ArrayList<String>();
		
		while(stream.incrementToken()) {

			System.out.println("charTermAtt : " + charTermAtt.toString());
			System.out.println("offsetAtt start offset : " + offsetAtt.startOffset());
			System.out.println("offsetAtt end offset : " + offsetAtt.endOffset());
			System.out.println("typeAtt : " + typeAtt.type());
			System.out.println("positionAtt : " + positionAtt.getPositionIncrement());

			Assert.assertTrue(nounsList.contains(charTermAtt.toString()));
			
			extractedNouns.add(charTermAtt.toString());
		}
		
		for(String syn : nounsList) {
			Assert.assertTrue(extractedNouns.contains(syn));
		}
	}
}
