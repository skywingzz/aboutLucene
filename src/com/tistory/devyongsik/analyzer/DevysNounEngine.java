package com.tistory.devyongsik.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 18.
 *
 */
public class DevysNounEngine implements Engine {

	private Log logger = LogFactory.getLog(DevysNounEngine.class);

	private static DevysNounEngine nounEngineInstance = new DevysNounEngine();

	//�������� �о�� ������ ���߿� ��ü�ϸ� �˴ϴ�.
	private List<String> nounDic = new ArrayList<String>();


	public static DevysNounEngine getInstance() {
		return nounEngineInstance;
	}

	private DevysNounEngine() {
		if(logger.isInfoEnabled())
			logger.info("������ �н��ϴ�.");

		nounDic.add("�ڹ�");
		nounDic.add("������");
		nounDic.add("�ϵ�");
		nounDic.add("����");
		nounDic.add("�ý���");
	}

	@Override
	public Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception {

		//���� ���ͷκ��� ����� Token�� �������� ����ϴ�.
		CharTermAttribute termAttr = attributeSource.getAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = attributeSource.getAttribute(TypeAttribute.class);
		//OffsetAttribute offSetAttr = attributeSource.getAttribute(OffsetAttribute.class);

		//word Ÿ�Ը� �м��Ѵ�.
		boolean isNotWord = false;
		if(typeAttr.type().equals("word")) isNotWord = false;

		char[] term = termAttr.buffer();

		if(logger.isDebugEnabled()) {
			logger.debug("char : " + new String(term));
			logger.debug("char length : " + term.length);
		}

		Stack<State> nounsStack = new Stack<State>();
		String comparedWord = "";
		
		if(isNotWord) {
			if(logger.isDebugEnabled()) {
				logger.debug("��� �м� ����� �ƴմϴ�.");
			}
			
			return nounsStack;
		}
		
		for(int startIndex = 0 ; startIndex < term.length; startIndex++) {
			for(int endIndex = 0; endIndex < term.length - startIndex; endIndex++) {
				comparedWord = new String(term, startIndex, endIndex);

				//��Ī�� �� State ����
				if(nounDic.contains(comparedWord)) {
					CharTermAttribute attr = attributeSource.addAttribute(CharTermAttribute.class); //������ ������ AttributeSource�� Attribute�� �޾ƿ�
					attr.setEmpty();
					attr.append(comparedWord);

					PositionIncrementAttribute positionAttr = attributeSource.addAttribute(PositionIncrementAttribute.class); 
					positionAttr.setPositionIncrement(1);  //����� ����̱� ������ ��ġ������ 1�� ����

					TypeAttribute typeAtt = attributeSource.addAttribute(TypeAttribute.class); 
					//Ÿ���� synonym���� �����Ѵ�. ���߿� ������� �� ���Ǿ� Ÿ���� �ǳʶٱ� ����
					typeAtt.setType("noun"); 

					//offset�� ������־�� �մϴ�. �׷��� ���̶������� �� �˴ϴ�.
					
					nounsStack.push(attributeSource.captureState()); //����� ���Ǿ ���� AttributeSource�� Stack�� ����
				}
			}
		}

		return nounsStack;
	}

}
