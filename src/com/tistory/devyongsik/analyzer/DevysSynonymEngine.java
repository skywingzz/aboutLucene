package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;
import org.apache.lucene.util.Version;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 8.
 *
 */
public class DevysSynonymEngine implements Engine {

	private Log logger = LogFactory.getLog(DevysSynonymEngine.class);

	private static RAMDirectory directory;
	private static IndexSearcher searcher;
	private static DevysSynonymEngine synonymEngineInstance = new DevysSynonymEngine();

	//�������� �о�� ������ ���߿� ��ü�ϸ� �˴ϴ�.
	private String synonymWord = new String("��Ʈ��,��Ʈ��pc,��Ʈ����ǻ��,��Ʈ���Ǿ�,notebook");

	public static DevysSynonymEngine getInstance() {
		return synonymEngineInstance;
	}

	private DevysSynonymEngine() {
		if(logger.isInfoEnabled())
			logger.info("���Ǿ� ������ �ǽ��մϴ�.");

		createSynonymIndex();

		if(logger.isInfoEnabled())
			logger.info("���Ǿ� ���� �Ϸ�");

		try {
			searcher = new IndexSearcher(directory);
		} catch (CorruptIndexException e) {
			logger.error("���Ǿ� ���ο� ���� Searcher ���� �� ���� �߻��� : " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("���Ǿ� ���ο� ���� Searcher ���� �� ���� �߻��� : " + e);
			e.printStackTrace();
		}
	}

	private void createSynonymIndex() {
		directory = new RAMDirectory();

		try {

			Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_31); //���� ������ �м� �� �� ��� �� Analyzer
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

			IndexWriter ramWriter = new IndexWriter(directory, iwc);
			String[] synonymWords = synonymWord.split(",");


			int recordCnt = 0;
			//���Ǿ���� ,�� �߶󳻾� �����մϴ�.
			//�ϳ��� document�� syn�̶�� �̸��� �ʵ带 ������ �߰��մϴ�.
			//���߿� syn=��Ʈ�� ���� �˻��Ѵٸ� �׶� ���� ��� Document�κ��� 
			//��� ���Ǿ� ����Ʈ�� ���� �� �ֽ��ϴ�.
			Document doc = new Document();
			for(int i = 0, size = synonymWords.length; i < size ; i++) {
				

				String fieldValue = synonymWords[i];
				Field field = new Field("syn",fieldValue,Store.YES,Index.NOT_ANALYZED);
				doc.add(field);

				recordCnt++;
			}//end for
			ramWriter.addDocument(doc);
			ramWriter.optimize();
			ramWriter.close();


			if(logger.isInfoEnabled())
				logger.info("���Ǿ� ���� �ܾ� ���� : " + recordCnt);

		} catch (CorruptIndexException e) {
			logger.error("���Ǿ� ���� �� ���� �߻��� : " + e);
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			logger.error("���Ǿ� ���� �� ���� �߻��� : " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("���Ǿ� ���� �� ���� �߻��� : " + e);
			e.printStackTrace();
		}
	}

	private List<String> getWords(String word) throws Exception {
		List<String> synWordList = new ArrayList<String>();
		if(logger.isDebugEnabled()) {
			logger.debug("���Ǿ� Ž�� : " + word);
		}

		Query query = new TermQuery(new Term("syn",word));
		
		if(logger.isDebugEnabled()) {
			logger.debug("query : " + query);
		}
		
		TopScoreDocCollector collector = TopScoreDocCollector.create(5 * 5, false);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		if(logger.isDebugEnabled()) {
			logger.debug("��� word : " + word);
			//�˻��� document�� �ϳ��̹Ƿ�..
			logger.debug("���Ǿ� ���� : " + hits.length);
		}

		for(int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);

			String[] values = doc.getValues("syn");

			for(int j = 0; j < values.length; j++) {
				if(logger.isDebugEnabled())
					logger.debug("��� word : " + "["+word+"]" + " ����� ���Ǿ� : " + values[j]);

				if(!word.equals(values[j])) {
					synWordList.add(values[j]);
				}
			}
		}
		return synWordList;
	}

	@Override
	public Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception {
		CharTermAttribute charTermAttr = attributeSource.getAttribute(CharTermAttribute.class);
		
		if(logger.isDebugEnabled())
			logger.debug("�Ѿ�� Term : " + charTermAttr.toString());
		
		Stack<State> synonymsStack = new Stack<State>();

		List<String> synonyms = getWords(charTermAttr.toString());

		if (synonyms.size() == 0) new Stack<State>(); //���Ǿ� ����

		for (int i = 0; i < synonyms.size(); i++) {
			
			//#1. ���Ǿ�� Ű���� ������ Type����, ��ġ���������� ����ǰ� ������ �Ӽ����� ������ �����ϱ� ������
			//attributeSource�κ��� ������ �ʿ��� ������ �����ͼ� �ʿ��� ������ �����Ѵ�.
			//offset�� ������ �����ϱ� ������ �ǵ帮�� �ʴ´�.
			CharTermAttribute attr = attributeSource.addAttribute(CharTermAttribute.class); //������ ������ AttributeSource�� Attribute�� �޾ƿ�
			attr.setEmpty();
			attr.append(synonyms.get(i));
			PositionIncrementAttribute positionAttr = attributeSource.addAttribute(PositionIncrementAttribute.class); //���� AttributeSource�� Attribute�� �޾ƿ�
			positionAttr.setPositionIncrement(0);  //���Ǿ��̱� ������ ��ġ���� ������ ����
			TypeAttribute typeAtt = attributeSource.addAttribute(TypeAttribute.class); //���� AttributeSource�� Attribute�� �޾ƿ�
			//Ÿ���� synonym���� �����Ѵ�. ���߿� ������� �� ���Ǿ� Ÿ���� �ǳʶٱ� ����
			typeAtt.setType("synonym"); 
			
			synonymsStack.push(attributeSource.captureState()); //����� ���Ǿ ���� AttributeSource�� Stack�� ����
		}
		
		return synonymsStack;
	}
}
