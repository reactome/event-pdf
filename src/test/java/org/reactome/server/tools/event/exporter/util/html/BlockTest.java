package org.reactome.server.tools.event.exporter.util.html;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.tools.event.exporter.BaseTest;
import org.reactome.server.tools.event.exporter.exception.DocumentExporterException;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.reactome.server.tools.event.exporter.util.HtmlUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class BlockTest extends BaseTest {

    @Autowired
    AdvancedDatabaseObjectService dos;

    @Disabled
    @Test
    public void testOne() {
        final String text =
                "<p>The P450 isozyme system is the major phase 1 biotransforming system in man, accounting for more than 90% of drug biotransformations. This system has huge catalytic versatility and a broad substrate specificity, acting upon xenobiotica and endogenous compounds. It is also called the mixed-function oxidase system,  the P450 monooxygenases and the heme-thiolate protein system. All P450 enzymes are a group of <i><b>heme-containing isozymes</b></i> which are located on the membrane of the smooth endoplasmic reticulum. They can be found in all tissues of the human body but are most concentrated in the liver. The name \"cytochrome P450\" (CYP) is derived from the spectral absorbance maximum at 450nm when carbon monoxide binds to CYP in its reduced (ferrous, Fe<sup>2+</sup>) state. The basic reaction catalyzed by CYP is <i><b>mono-oxygenation</b></i>, that is the transfer of one oxygen atom from molecular oxygen to a substrate. The other oxygen atom is reduced to water during the reaction with the equivalents coming from the cofactor NADPH. The basic reaction is;</p><b><p align=center> RH (substrate) + O<sub>2</sub> + NADPH + H<sup>+</sup> = ROH (product) + H<sub>2</sub>O + NADP<sup>+</sup></p></b><p>The end results of this reaction can be (N-)hydroxylation, epoxidation, heteroatom (N-, S-) oxygenation, heteroatom (N-, S-, O-) dealkylation, ester cleavage, isomerization, dehydrogenation, replacement by oxygen or even reduction under anaerobic conditions.</p><p>The metabolites produced from these reactions can either be mere intermediates which have relatively little reactivity towards cellular sysytems and are readily conjugated, or, they can be disruptive to cellular systems. Indeed, inert compounds need to be prepared for conjugation and thus the formation of potentially reactive metabolites is in most cases unavoidable.</p><p>There are 57 human CYPs (in 18 families and 42 subfamilies), mostly concentrated in the endoplasmic reticulum of liver cells although many tissues have them to some extent (Nelson DR et al, 2004). CYPs are grouped into 14 families according to their sequence similarity. Generally, enzymes in the same family share 40% sequence similarity and 55% within a subfamily. Nomenclature of P450s is as follows. CYP (to represent cytochrome P450 superfamily), followed by an arabic number for the family, a capital letter for the subfamily and finally a second arabic number to denote the isoenzyme. An example is CYP1A1 which is the first enzyme in subfamily A of cytochrome P450 family 1.</p><p>The majority of the enzymes are present in the CYP1-4 families. CYP1-3 are primarily concerned with xenobiotic biotransformation whereas the other CYPs deal primarily with endogenous compounds. The CYP section is structured by the typical substrate they act upon. Of the 57 human CYPs, 7 encode mitochondrial enzymes, all involved in sterol biosynthesis. Of the remaining 50 microsomal enzymes, 20 act upon endogenous compounds, 15 on xenobiotics and 15 are the so-called \"orphan enzymes\" with no substrate identified.</p><p>The P450 catalytic cycle <i>(picture)</i> shows the steps involved when a substrate binds to the enzyme.</p><p><font color=red>(1)</font> The normal state of a P450 with the iron in its ferric [Fe<sup>3+</sup>] state.</p><p><font color=red>(2)</font> The substrate binds to the enzyme.</p><p><font color=red>(3)</font> The enzyme is reduced to the ferrous [Fe<sup>2+</sup>] state by the addition of an electron from NADPH cytochrome P450 reductase. The bound substrate facilitates this process.</p><p><font color=red>(4,5)</font> Molecular oxygen binds and forms an Fe<sup>2+</sup>OOH complex with the addition of a proton and a second donation of an electron from either NADPH cytochrome P450 reductase or cytochrome b5. A second proton cleaves the Fe<sup>2+</sup>OOH complex to form water.</p><p><font color=red>(6)</font> An unstable [FeO]<sup>3+</sup> complex donates its oxygen to the substrate <font color=red>(7)</font>. The oxidised substrate is released and the enzyme returns to its initial state <font color=red>(1)</font>.</p>";
        final PdfProfile profile = loadProfile("breathe");
        try (Document document = new Document(new PdfDocument(new PdfWriter("output.pdf")))) {
            document.add(profile.getParagraph(text).setFontSize(profile.getFontSize() - 1).setFontColor(new DeviceGray(0.5f)));
            try {
                HtmlUtils.getElements(text, profile).forEach(document::add);
            } catch (Exception e) {
                System.err.println(text);
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
    }

    @Disabled
    @Test
    public void testBlocks() {
        // runs through all of the summations in Reactome and tries to use the HtmlProcessor on them
        final PdfProfile profile = loadProfile("breathe");
        try (Document document = new Document(new PdfDocument(new PdfWriter("output.pdf")))) {
            final Collection<String> suumm = dos.getCustomQueryResults(String.class, "MATCH (s:Summation) RETURN DISTINCT s.text");
            for (String text : suumm) {
//				boolean hasAny = false;
//				for (String tag : Arrays.asList("<a", "<b>", "<strong>", "<em>", "<i>", "<ul>", "<li>", "<ol>", "<sup>", "<sub>", "<font")) {
//					if (text.contains(tag)) {
//						hasAny = true;
//						break;
//					}
//				}
//				if (!hasAny) continue;
                document.add(profile.getParagraph(text).setFontSize(profile.getFontSize() - 1).setFontColor(new DeviceGray(0.5f)));
                try {
                    HtmlUtils.getElements(text, profile).forEach(document::add);
                } catch (Exception e) {
                    System.err.println(text);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException | CustomQueryException e) {
            e.printStackTrace();

        }
    }

    // TODO: 19/12/18 too weak profile system
    private PdfProfile loadProfile(String profile) throws DocumentExporterException {
        final ObjectMapper MAPPER = new ObjectMapper();
        try {
            final InputStream resource = getClass().getResourceAsStream("/profiles/" + profile.toLowerCase() + ".json");
            return MAPPER.readValue(resource, PdfProfile.class);
        } catch (IOException e) {
            throw new DocumentExporterException("Couldn't load profile " + profile, e);
        }
    }
}