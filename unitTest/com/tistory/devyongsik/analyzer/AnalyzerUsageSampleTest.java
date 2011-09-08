package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.Assert;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class AnalyzerUsageSampleTest {
	
	@Test
	public void whiteSpaceAnalyzerSample() throws IOException {
		StringReader stringReader = new StringReader("������ ��α׸� �ۼ��մϴ�.");
		Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_32);
		TokenStream tokenStream = analyzer.reusableTokenStream("title", stringReader);
		
		CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
		PositionIncrementAttribute posIncrAtt = tokenStream.addAttribute(PositionIncrementAttribute.class);
		OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
	    
	    while (tokenStream.incrementToken()) {
	      String text = termAtt.toString();
	      int postIncrAttr = posIncrAtt.getPositionIncrement();
	      int startOffSet = offsetAtt.startOffset();
	      int endOffSet = offsetAtt.endOffset();
	      
	      System.out.println("text : "+ text);
	      System.out.println("postIncrAttr : " + postIncrAttr);
	      System.out.println("startOffSet : " + startOffSet);
	      System.out.println("endOffSet : " + endOffSet);
	      
	      Assert.assertNotNull(text);
	      Assert.assertTrue(postIncrAttr > 0);
	      Assert.assertTrue(startOffSet >= 0);
	      Assert.assertTrue(endOffSet > 0);
	    }
	}
}
