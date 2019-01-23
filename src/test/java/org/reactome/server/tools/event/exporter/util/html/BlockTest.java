package org.reactome.server.tools.event.exporter.util.html;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.junit.Test;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.tools.event.exporter.BaseTest;
import org.reactome.server.tools.event.exporter.exception.DocumentExporterException;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BlockTest extends BaseTest {

	@Autowired
	AdvancedDatabaseObjectService dos;

	@Test
	public void testOne() {
		final String text =
				"<p>The P450 isozyme system is the major phase 1 biotransforming system in man, accounting for more than 90% of drug biotransformations. This system has huge catalytic versatility and a broad substrate specificity, acting upon xenobiotica and endogenous compounds. It is also called the mixed-function oxidase system,  the P450 monooxygenases and the heme-thiolate protein system. All P450 enzymes are a group of <i><b>heme-containing isozymes</b></i> which are located on the membrane of the smooth endoplasmic reticulum. They can be found in all tissues of the human body but are most concentrated in the liver. The name \"cytochrome P450\" (CYP) is derived from the spectral absorbance maximum at 450nm when carbon monoxide binds to CYP in its reduced (ferrous, Fe<sup>2+</sup>) state. The basic reaction catalyzed by CYP is <i><b>mono-oxygenation</b></i>, that is the transfer of one oxygen atom from molecular oxygen to a substrate. The other oxygen atom is reduced to water during the reaction with the equivalents coming from the cofactor NADPH. The basic reaction is;</p><b><p align=center> RH (substrate) + O<sub>2</sub> + NADPH + H<sup>+</sup> = ROH (product) + H<sub>2</sub>O + NADP<sup>+</sup></p></b><p>The end results of this reaction can be (N-)hydroxylation, epoxidation, heteroatom (N-, S-) oxygenation, heteroatom (N-, S-, O-) dealkylation, ester cleavage, isomerization, dehydrogenation, replacement by oxygen or even reduction under anaerobic conditions.</p><p>The metabolites produced from these reactions can either be mere intermediates which have relatively little reactivity towards cellular sysytems and are readily conjugated, or, they can be disruptive to cellular systems. Indeed, inert compounds need to be prepared for conjugation and thus the formation of potentially reactive metabolites is in most cases unavoidable.</p><p>There are 57 human CYPs (in 18 families and 42 subfamilies), mostly concentrated in the endoplasmic reticulum of liver cells although many tissues have them to some extent (Nelson DR et al, 2004). CYPs are grouped into 14 families according to their sequence similarity. Generally, enzymes in the same family share 40% sequence similarity and 55% within a subfamily. Nomenclature of P450s is as follows. CYP (to represent cytochrome P450 superfamily), followed by an arabic number for the family, a capital letter for the subfamily and finally a second arabic number to denote the isoenzyme. An example is CYP1A1 which is the first enzyme in subfamily A of cytochrome P450 family 1.</p><p>The majority of the enzymes are present in the CYP1-4 families. CYP1-3 are primarily concerned with xenobiotic biotransformation whereas the other CYPs deal primarily with endogenous compounds. The CYP section is structured by the typical substrate they act upon. Of the 57 human CYPs, 7 encode mitochondrial enzymes, all involved in sterol biosynthesis. Of the remaining 50 microsomal enzymes, 20 act upon endogenous compounds, 15 on xenobiotics and 15 are the so-called \"orphan enzymes\" with no substrate identified.</p><p>The P450 catalytic cycle <i>(picture)</i> shows the steps involved when a substrate binds to the enzyme.</p><p><font color=red>(1)</font> The normal state of a P450 with the iron in its ferric [Fe<sup>3+</sup>] state.</p><p><font color=red>(2)</font> The substrate binds to the enzyme.</p><p><font color=red>(3)</font> The enzyme is reduced to the ferrous [Fe<sup>2+</sup>] state by the addition of an electron from NADPH cytochrome P450 reductase. The bound substrate facilitates this process.</p><p><font color=red>(4,5)</font> Molecular oxygen binds and forms an Fe<sup>2+</sup>OOH complex with the addition of a proton and a second donation of an electron from either NADPH cytochrome P450 reductase or cytochrome b5. A second proton cleaves the Fe<sup>2+</sup>OOH complex to form water.</p><p><font color=red>(6)</font> An unstable [FeO]<sup>3+</sup> complex donates its oxygen to the substrate <font color=red>(7)</font>. The oxidised substrate is released and the enzyme returns to its initial state <font color=red>(1)</font>.</p>";
		final PdfProfile profile = loadProfile("breathe");
		try (Document document = new Document(new PdfDocument(new PdfWriter("output.pdf")))) {
			document.add(profile.getParagraph(text).setFontSize(profile.getFontSize() - 1).setFontColor(new DeviceGray(0.5f)));
			try {
				HtmlProcessor.add(document, text, profile);
			} catch (Exception e) {
				System.err.println(text);
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		}

	}

	@Test
	public void testBlocks() {
		final List<String> texts = Arrays.asList(
//				"Activator protein-1 (AP-1) is a collective term referring to a group of transcription factors that bind to promoters of target genes in a sequence-specific manner. AP-1 family consists of hetero- and homodimers of bZIP (basic region leucine zipper) proteins, mainly of Jun-Jun, Jun-Fos or Jun-ATF. <p>AP-1 members are involved in the regulation of a number of cellular processes including cell growth, proliferation, survival, apoptosis, differentiation, cell migration. The ability of a single transcription factor to determine a cell fate critically depends on the relative abundance of AP-1 subunits, the composition of AP-1 dimers, the quality of stimulus, the cell type, the co-factor assembly. </p><p>AP-1 activity is regulated on multiple levels; transcriptional, translational and post-translational control mechanisms contribute to the balanced production of AP-1 proteins and their functions. Briefly, regulation occurs through:<ol><li>effects on jun, fos, atf gene transcription and mRNA turnover.<li> AP-1 protein members turnover. <li>post-translational modifications of AP-1 proteins that modulate their transactivation potential (effect of protein kinases or phosphatases).<li>interactions with other transcription factors that can either induce or interfere with AP-1 activity.</ol>",
//				"Caspases, a family of cysteine proteases, execute apoptotic cell death. Caspases exist as inactive zymogens in cells and undergo a cascade of catalytic activation at the onset of apoptosis. Initiation of apoptosis occurs through either a cell-intrinsic or cell-extrinsic pathway. Extrinsic pathway cell death signals originate at the plasma membrane where:<ul><li>An extracellular ligand (e.g., FasL) binds to its cell surface transmembrane “death receptor” (e.g., Fas receptor), inducing oligomerization of the receptor (Trauth et al. 1989; Itoh and Nagata 1993; Danial and Korsmeyer 2004). The \"death receptors\" are specialized cell-surface receptors including Fas/CD95, tumor necrosis factor-alpha (TNF-alpha) receptor 1, and two receptors, DR4 and DR5, that bind to the TNF-alpha related apoptosis-inducing ligand (TRAIL). Ligand binding promotes clustering of proteins that bind to the intracellular domain of the receptor (e.g., FADD, or Fas-associated death domain-containing protein), which then binds to the prodomain of initiator caspases (e.g.caspase-8 or -10) to promote their dimerization and activation. Active caspase-8/-10 can then directly cleave and activate effector caspases, such as caspase-3 or it can cleave Bid, which facilitates mitochondrial cytochrome c release.</li><li>Unique group of proteins termed dependence receptors (DpRs) transduce positive (often prosurvival or progrowth) signals when engaged by ligand, but emit proapoptotic signals in the absence of ligand (Goldschneider and Mehlen 2010). DpR family includes p75 neurotrophin receptor (p75NTR), deleted in colon cancer (DCC), and UNC5 homologs, among others. cell-surface membrane receptors.</li></ul>",
//				"The mitogen activated protein kinase (MAPK) cascade, one of the most ancient and evolutionarily conserved signaling pathways, is involved in many processes of immune responses. The MAP kinases cascade transduces signals from the cell membrane to the nucleus in response to a wide range of stimuli (Chang and Karin, 2001; Johnson et al, 2002). <p>There are three major groups of MAP kinases<ul><li>the extracellular signal-regulated protein kinases ERK1/2, <li>the p38 MAP kinase<li> and the c-Jun NH-terminal kinases JNK.</ul><p>ERK1 and ERK2 are activated in response to growth stimuli. Both JNKs and p38-MAPK are activated in response to a variety of cellular and environmental stresses. The MAP kinases are activated by dual phosphorylation of Thr and Tyr within the tripeptide motif Thr-Xaa-Tyr. The sequence of this tripeptide motif is different in each group of MAP kinases: ERK (Thr-Glu-Tyr); p38 (Thr-Gly-Tyr); and JNK (Thr-Pro-Tyr).<p>MAPK activation is mediated by signal transduction in the conserved three-tiered kinase cascade: MAPKKKK (MAP4K or MKKKK or MAPKKK Kinase) activates the MAPKKK. The MAPKKKs then phosphorylates a dual-specificity protein kinase MAPKK, which in turn phosphorylates the MAPK.<p>The dual specificity MAP kinase kinases (MAPKK or MKK) differ for each group of MAPK. The ERK MAP kinases are activated by the MKK1 and MKK2; the p38 MAP kinases are activated by MKK3, MKK4, and MKK6; and the JNK pathway is activated by MKK4 and MKK7. The ability of MAP kinase kinases (MKKs, or MEKs) to recognize their cognate MAPKs is facilitated by a short docking motif (the D-site) in the MKK N-terminus, which binds to a complementary region on the MAPK. MAPKs then recognize many of their targets using the same strategy, because many MAPK substrates also contain D-sites.<p>The upstream signaling events in the TLR cascade that initiate and mediate the ERK signaling pathway remain unclear.",
//				"Complex I (NADH:ubiquinone oxidoreductase or NADH dehydrogenase) utilizes NADH formed from glycolysis and the TCA cycle to pump protons out of the mitochondrial matrix. It is the largest enzyme complex in the electron transport chain, containing 45 subunits. Seven subunits (ND1-6, ND4L) are encoded by mitochondrial DNA (Loeffen et al [1998]), the remainder are encoded in the nucleus. The enzyme has a FMN prosthetic group and 8 Iron-Sulfur (Fe-S) clusters. The electrons from NADH oxidation pass through the flavin (FMN) and Fe-S clusters to ubiquinone (CoQ). This electron transfer is coupled with the translocation of protons from the mitochondrial matrix to the intermembrane space. For each electron transferred, 2 protons can be pumped out of the matrix. As there are 2 electrons transferred, 4 protons can be pumped out.<br>Complex I is made up of 3 sub-complexes - Iron-Sulfur protein fraction (IP), Flavoprotein fraction (FP) and the Hydrophobic protein fraction (HP), probably arranged in an L-shaped structure with the IP and FP fractions protruding into the mitochondrial matrix and the HP arm lying within the inner mitochondrial membrane. The overall reaction can be summed as below:<br><b>NADH + Ubiquinone + 5H+ (mito. matrix) = NAD+ + Ubiquinol + 4H+ (intermemb. space)<br></b>The electrons from complex I are transferred to ubiquinone (Coenzyme Q, CoQ), a small mobile carrier of electrons located within the inner membrane. Ubiquinone is reduced to ubiquinol (QH2) during this process.<br><br>Mitochondrial coenzyme Q-binding protein COQ10 homologs A and B (COQ10A and B) are thought to be required for correct coenzyme CoQ in the respiratory chain. Their function in humans is unknown but the yeast model suggests functions in facilitating de novo CoQ biosynthesis and in delivering it to one or more complexes of the respiratory electron transport chain (Barros et al. 2005, Allan et al. 2013).",
				"The events of replication of the genome and the subsequent segregation of chromosomes into daughter cells make up the cell cycle. DNA replication is carried out during a discrete temporal period known as the S (synthesis)-phase, and chromosome segregation occurs during a massive reorganization of cellular architecture at mitosis. Two gap-phases separate these cell cycle events: G1 between mitosis and S-phase, and G2 between S-phase and mitosis. Cells can exit the cell cycle for a period and enter a quiescent state known as G0, or terminally differentiate into cells that will not divide again, but undergo morphological development to carry out the wide variety of specialized functions of individual tissues.<p>A family of protein serine/threonine kinases known as the cyclin-dependent kinases (CDKs) controls progression through the cell cycle. As the name suggests, the kinase activity of the catalytic subunits is dependent on binding to cyclin partners, and control of cyclin abundance is one of several mechanisms by which CDK activity is regulated throughout the cell cycle. <p>A complex network of regulatory processes determines whether a quiescent cell (in G0 or early G1) will leave this state and initiate the processes to replicate its chromosomal DNA and divide. This regulation, during the <b>Mitotic G1-G1/S phases</b> of the cell cycle, centers on transcriptional regulation by the DREAM complex, with major roles for D and E type cyclin proteins.<p>Chromosomal DNA synthesis occurs in the <b>S phase</b>, or the synthesis phase, of the cell cycle. The cell duplicates its hereditary material, and two copies of each chromosome are formed. A key aspect of the <b>regulation of DNA</b> replication is the assembly and modification of a pre-replication complex assembled on ORC proteins.<p><b>Mitotic G2-G2/M phases</b> encompass the interval between the completion of DNA synthesis and the beginning of mitosis. During G2, the cytoplasmic content of the cell increases. At G2/M transition, duplicated centrosomes mature and separate and CDK1:cyclin B complexes become active, setting the stage for spindle assembly and chromosome condensation at the start of mitotic <b>M phase</b>. Mitosis, or M phase, results in the generation of two daughter cells each with a complete diploid set of chromosomes. Events of the <b>M/G1 transition</b>, progression out of mitosis and division of the cell into two daughters (cytokinesis) are regulated by the Anaphase Promoting Complex.<p>The Anaphase Promoting Complex or Cyclosome (APC/C) plays additional roles in <b>regulation of the mitotic cell cycle</b>, insuring the appropriate length of the G1 phase. The APC/C itself is regulated by phosphorylation and interactions with checkpoint proteins."
		);
		final PdfProfile profile = loadProfile("breathe");
		try (Document document = new Document(new PdfDocument(new PdfWriter("output.pdf")))) {
			final Collection<String> suumm = dos.getCustomQueryResults(String.class, "MATCH (s:Summation) RETURN s.text");
			for (String text : suumm) {
				document.add(profile.getParagraph(text).setFontSize(profile.getFontSize() - 1).setFontColor(new DeviceGray(0.5f)));
				try {
					HtmlProcessor.add(document, text, profile);
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