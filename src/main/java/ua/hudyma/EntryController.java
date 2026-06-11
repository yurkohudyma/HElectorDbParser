package ua.hudyma;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EntryController {
    private final EntryService entryService;
    @GetMapping
    public void getNodeList (@RequestParam String uri) throws ParserConfigurationException, IOException, SAXException {
        entryService.readNodeList(uri);
    }

    @GetMapping("/ageStats")
    public ResponseEntity<AgeStatsDto> getAgeStats (){
        return ResponseEntity.ok(entryService.getAgeStats());
    }
    @GetMapping("/getEntryListByYears")
    public ResponseEntity<List<Entry>> getEntryListByYears (@RequestParam Long years){
        return ResponseEntity.ok(entryService.findEntryByBirthYears(years));
    }
    @GetMapping("/getNameFreqMap")
    public ResponseEntity<Map<String,Long>> getNamesFreqMap (){
        return ResponseEntity.ok(entryService.getNameFrequencyMap());
    }

    @GetMapping("/getMaxAge")
    public ResponseEntity<List<Entry>> findEntriesWithMaxAge(){
        return ResponseEntity.ok(entryService.findEntriesWithMaxAge());
    }
}
