package me.retrotv.bookmanagement.domain.naver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import me.retrotv.bookmanagement.exception.NoSuchApiElementException;
import me.retrotv.bookmanagement.exception.XmlParseErrorException;
import me.retrotv.bookmanagement.response.BasicResult;

@Service
public class NaverService {
    public BasicResult searchBookByIsbn(String isbn) {
        URI uri = UriComponentsBuilder.fromUriString("https://openapi.naver.com")
                                      .path("/v1/search/book_adv.xml")
                                      .queryParam("d_isbn", isbn)
                                      .encode()
                                      .build()
                                      .toUri();

        RestTemplate restTemplate = new RestTemplate();

        RequestEntity<Void> req = RequestEntity.get(uri)
                                               .header("X-Naver-Client-Id", "")
                                               .header("X-Naver-Client-Secret", "")
                                               .build();

        ResponseEntity<String> result = restTemplate.exchange(req, String.class);

        // 조회한 데이터를 파싱해서 total 태그 안의 값이 0이면 조회된 책이 없다고 알림.
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();

            String body = null;
            if(result.hasBody()) {
                body = result.getBody();
            }

            byte[] bytes = null;
            if(body != null) {
                bytes = body.getBytes();
            }

            try(InputStream is = new ByteArrayInputStream(bytes)) {
                Document document = documentBuilder.parse(is);
                Element element = document.getDocumentElement();
                NodeList items = element.getElementsByTagName("total");

                int n = items.getLength();
                for(int i = 0; i < n; i++) {
                    Node item = items.item(i);
                    Node text = item.getFirstChild();
                    String total = text.getNodeValue();

                    if("0".equals(total)) { throw new NoSuchApiElementException("조회된 책이 없습니다."); }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException exception) {
            throw new XmlParseErrorException("책을 조회하는 도중 오류가 발생했습니다.\n잠시 뒤에 다시 시도해 주세요.");
        }

        return new BasicResult("책 조회가 완료 되었습니다.", result.getBody());
    }
}
