package fr.cnrs.opentheso.bean.importexport.newcsvimport;

import fr.cnrs.opentheso.models.skos.SkosConceptDto;
import fr.cnrs.opentheso.services.imports.csv.newcodes.CsvImportService;
import fr.cnrs.opentheso.services.mappers.SkosConceptMapper;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVRecord;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("skosConceptImportBean")
@SessionScoped
@Getter
@Setter
public class SkosConceptImportBean implements Serializable {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private SkosConceptMapper skosConceptMapper; // <-- assembler centralisé

    private UploadedFile file;
    private List<SkosConceptDto> importedConcepts = new ArrayList<>();
    private String message;

    /**
     * Gestion du fichier CSV uploadé.
     * Lis le CSV, mappe vers SkosConceptDto, puis normalise chaque DTO via l'assembler.
     */
    public void handleUpload() {
        importedConcepts.clear();
        message = "";

        if (file == null) {
            message = "Aucun fichier sélectionné.";
            return;
        }

        try (InputStream is = file.getInputStream()) {

            // Lire CSV avec détection BOM UTF-8
            List<CSVRecord> records = csvImportService.readCsv(is);

            // Mapper vers SkosConceptDto
            importedConcepts = csvImportService.mapCsvToSkosConceptDto(records);

            // Normalisation via l'assembler (relations, notes, labels, images)
            importedConcepts.forEach(skosConceptMapper::finalizeMapping);

            message = "Fichier importé avec succès : " + importedConcepts.size() + " concepts lus.";

        } catch (IOException e) {
            e.printStackTrace();
            message = "Erreur lors de la lecture du fichier : " + e.getMessage();
        }
    }
}