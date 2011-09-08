package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 18.
 *
 */
public class DevysNounFilter extends TokenFilter {
	private Log logger = LogFactory.getLog(DevysNounFilter.class);

	private Stack<State> nouns = new Stack<State>();
	private Engine engine;

	public DevysNounFilter(TokenStream in) {
		super(in);
		this.engine = DevysNounEngine.getInstance();
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(logger.isDebugEnabled())
			logger.debug("incrementToken DevysNounFilter");


		if (nouns.size() > 0) {
			if(logger.isDebugEnabled())
				logger.debug("��� Stack���� ��ū ������");

			State synState = nouns.pop();
			restoreState(synState); //#3. ������ stream �� AttributeSource�� �����س��� ������ �ٲ�ġ���Ѵ�.

			return true;
		}

		if (!input.incrementToken())
			return false;
		
		try {
			nouns = engine.getAttributeSources(input.cloneAttributes());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		//���� Token ����
		if(logger.isDebugEnabled()) {
			CharTermAttribute charTermAttr = input.getAttribute(CharTermAttribute.class);
			logger.debug("���� termAttr ���� : [" + charTermAttr.toString() + "]");
		}
		
		return true;
	}
}
