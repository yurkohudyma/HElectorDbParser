package ua.hudyma;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Log4j2
public class EntryService {
    private final EntryRepository entryRepository;
    private final static String url = "S:/DOX/_DB/Election_reg/dump/";

    public Map<String, Long> getNameFrequencyMap() {
        return entryRepository.findAll()
                .stream()
                .filter(Objects::nonNull)
                .collect(groupingBy(
                        Entry::getName,
                        counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                        .reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public AgeStatsDto getAgeStats() {
        var list = getYearsList();
        var min = getMin(list);
        var max = getMax(list);
        var avg = (min + max) / 2;
        var mediane = getMediane(list);
        return new AgeStatsDto(max, avg, min, mediane);
    }

    public List<Entry> findEntryByBirthYears(long years) {
        return entryRepository
                .findAll()
                .stream()
                .filter(entry -> getYearsToCurrent(
                        parseDate(entry
                                .getBirth())).equals(years))
                .toList();
    }

    public List<Entry> findEntriesWithMaxAge() {
        return entryRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> getYearsToCurrent(parseDate(entry.getBirth()))
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(List.of());
    }


    private List<Long> getYearsList() {
        return entryRepository
                .findAll()
                .stream()
                .map(Entry::getBirth)
                .map(this::parseDate)
                .map(this::getYearsToCurrent)
                .toList();
    }

    private static Long getMax(List<Long> list) {
        return list.stream()
                .max(Comparator.naturalOrder())
                .orElse(0L);
    }

    private static Long getMin(List<Long> list) {
        return list
                .stream()
                .min(Comparator.naturalOrder())
                .orElse(0L);
    }

    private Long getMediane(List<Long> list) {
        var sortedList = list.stream().sorted().toList();
        return sortedList.get(sortedList.size() / 2);
    }

    private LocalDateTime parseDate(String birthDate) {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(birthDate, formatter).atStartOfDay();
    }

    private Long getYearsToCurrent(LocalDateTime birthDate) {
        return birthDate.until(LocalDateTime.now(), ChronoUnit.YEARS);
    }

    public void readNodeList(String fileName) throws ParserConfigurationException, IOException, SAXException {
        var file = new File(url + fileName);
        var nodeList = loadDocument(file).getElementsByTagName("b2");
        var entryList = new ArrayList<Entry>();
        for (int node = 0; node < nodeList.getLength(); node++) {
            var b2Node = nodeList.item(node);
            var childNodeList = b2Node.getChildNodes();
            var entry = new Entry();
            for (int childnode = 0; childnode < childNodeList.getLength(); childnode++) {
                var child = childNodeList.item(childnode);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                var nodeName = child.getNodeName();
                var content = getChildNodeAttribContent(child);
                switch (nodeName) {
                    case "m2" -> entry.setBirth(content);
                    case "m3" -> entry.setAddress(content);
                    case "m4" -> entry.setMiddleName(content);
                    case "m5" -> entry.setSurname(content);
                    case "m6" -> entry.setName(content);
                    default -> log.error("child {} not identified", nodeName);
                }
            }
            entryList.add(entry);
        }
        entryRepository.saveAll(entryList);
    }

    private static String getChildNodeAttribContent(Node childnode) {
        var element = (Element) childnode;
        return element.getAttribute("u");
    }

    private static Document loadDocument(File file) throws ParserConfigurationException, IOException, SAXException {
        var factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().parse(file);
    }
}
