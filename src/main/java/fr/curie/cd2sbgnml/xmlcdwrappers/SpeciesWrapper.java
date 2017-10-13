package fr.curie.cd2sbgnml.xmlcdwrappers;

import org.sbml.x2001.ns.celldesigner.CelldesignerClassDocument.CelldesignerClass;
import org.sbml.x2001.ns.celldesigner.CelldesignerComplexSpeciesAliasDocument.CelldesignerComplexSpeciesAlias;
import org.sbml.x2001.ns.celldesigner.CelldesignerComplexSpeciesDocument.CelldesignerComplexSpecies;
import org.sbml.x2001.ns.celldesigner.CelldesignerModificationDocument.CelldesignerModification;
import org.sbml.x2001.ns.celldesigner.CelldesignerModificationResidueDocument.CelldesignerModificationResidue;
import org.sbml.x2001.ns.celldesigner.CelldesignerProteinDocument.CelldesignerProtein;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesAliasDocument.CelldesignerSpeciesAlias;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesDocument.CelldesignerSpecies;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesIdentityDocument.CelldesignerSpeciesIdentity;
import org.sbml.x2001.ns.celldesigner.CelldesignerStateDocument.CelldesignerState;
import org.sbml.x2001.ns.celldesigner.SpeciesDocument.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * wraps species and includedSpecies as they have a lot in common
 */
public class SpeciesWrapper {

    private final Logger logger = LoggerFactory.getLogger(SpeciesWrapper.class);

    private boolean isIncludedSpecies;
    private boolean isComplex;

    private String id;
    private String name;
    private String compartment;
    private String complex;
    private String cdClass;
    private int multimer;
    private String unitOfInformation;

    private List<AliasWrapper> aliases;
    private List<ResidueWrapper> residues;

    public SpeciesWrapper(Species species, ModelWrapper modelW) {
        this.isIncludedSpecies = false;
        CelldesignerClass cdClassClass = species.getAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerClass();
        this.cdClass = cdClassClass.getDomNode().getChildNodes().item(0).getNodeValue();
        this.id = species.getId();
        this.name = species.getName().getStringValue();
        this.compartment = species.getCompartment();
        this.complex = null;

        this.commonConstructor(species.getAnnotation().getCelldesignerSpeciesIdentity(), modelW);

    }

    public SpeciesWrapper(CelldesignerSpecies species, ModelWrapper modelW) {
        this.isIncludedSpecies = true;
        CelldesignerClass cdClassClass = species.getCelldesignerAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerClass();
        this.cdClass = cdClassClass.getDomNode().getChildNodes().item(0).getNodeValue();
        this.id = species.getId();
        this.name = species.getName().getStringValue();
        CelldesignerComplexSpecies complexSpecies = species.getCelldesignerAnnotation().getCelldesignerComplexSpecies();
        this.complex = complexSpecies.getDomNode().getChildNodes().item(0).getNodeValue();
        this.compartment = null;

        this.commonConstructor(species.getCelldesignerAnnotation().getCelldesignerSpeciesIdentity(), modelW);
    }

    private void commonConstructor(CelldesignerSpeciesIdentity identity, ModelWrapper modelW) {
        this.multimer = 1; // default to 1 if nothing else found
        this.residues = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.isComplex = this.cdClass.equals("COMPLEX");


        if(this.isComplex) {
            logger.debug("Species has "+modelW.getComplexSpeciesAliasFor(this.id).size()+" complexSpeciesAliases");
            for(CelldesignerComplexSpeciesAlias complexAlias : modelW.getComplexSpeciesAliasFor(this.id)) {
                if (complexAlias == null) {
                    continue;
                }
                logger.debug("Parse complex alias: " + complexAlias.getId());
                this.aliases.add(new AliasWrapper(complexAlias, this));
            }
        }
        //else {
        /*
         * here we shouldn't have to check normal aliases after complex aliases.
         * normally complex species should only have complex aliases, and not additional normal aliases
         * this only happens in ACSN
         */
        if(modelW.getSpeciesAliasFor(this.id) != null) {
            if(this.isComplex) {
                logger.warn("Complex species: "+this.id+" shouldn't have non-complex aliases");
            }

            logger.debug("Species has "+modelW.getSpeciesAliasFor(this.id).size()+" speciesAliases");
            for(CelldesignerSpeciesAlias alias : modelW.getSpeciesAliasFor(this.id)) {
                if (alias == null) {
                    continue;
                }
                logger.debug("Parse alias: " + alias.getId());
                this.aliases.add(new AliasWrapper(alias, this));
            }
        }

        // get information from the species' reference
        List<CelldesignerModificationResidue> listOfReferenceModif;
        HashMap<String, ResidueWrapper> mapOfReferenceModif = new HashMap<>();
        if(identity.isSetCelldesignerProteinReference()) {
            String protId = identity.getCelldesignerProteinReference().getDomNode().getChildNodes().item(0).getNodeValue();
            CelldesignerProtein prot = modelW.getProtein(protId);

            if(prot.isSetCelldesignerListOfModificationResidues()) {
                // loop through reference residues
                listOfReferenceModif =
                        Arrays.asList(prot.getCelldesignerListOfModificationResidues().getCelldesignerModificationResidueArray());

                mapOfReferenceModif = new HashMap<>();
                for (CelldesignerModificationResidue modif : listOfReferenceModif) {
                    System.out.println("Residue found for "+prot.getId()+" resid "+modif.getId()+" angle "+modif.getAngle());
                    ResidueWrapper residueWrapper = new ResidueWrapper(modif.getId());
                    residueWrapper.angle = Float.parseFloat(modif.getAngle());
                    if(modif.isSetName()) {
                        residueWrapper.name = modif.getName().getStringValue();
                    }
                    mapOfReferenceModif.put(residueWrapper.id, residueWrapper);
                }
                System.out.println(mapOfReferenceModif.size()+" res for protein "+protId);
                logger.debug(mapOfReferenceModif.size()+" res for protein "+protId);
            }
        }


        if(identity.isSetCelldesignerState()) {
            CelldesignerState state = identity.getCelldesignerState();

            // parse multimer and infounit
            if(state.isSetCelldesignerHomodimer()) {
                this.multimer = Integer.parseInt(state.getCelldesignerHomodimer().
                        getDomNode().getChildNodes().item(0).getNodeValue());
            }

            if(state.isSetCelldesignerListOfStructuralStates()) {
                // assume that there is only 1 state per species
                this.unitOfInformation = state.getCelldesignerListOfStructuralStates().
                        getCelldesignerStructuralStateArray(0).getStructuralState().getStringValue();
            }

            // parse state variable/residues
            if(state.isSetCelldesignerListOfModifications() && identity.isSetCelldesignerProteinReference()) {

                // list and map this species' residues
                List<CelldesignerModification> listOfModif =
                        Arrays.asList(state.getCelldesignerListOfModifications().getCelldesignerModificationArray());


                    // loop through the species' residues
                    for (CelldesignerModification modif : listOfModif) {
                        System.out.println("adding state: "+modif.getState().getStringValue()+" for res "+modif.getResidue());
                        String residueId = modif.getResidue();
                        ResidueWrapper residueWrapper = mapOfReferenceModif.get(residueId);
                        /*
                        In ACSN, residueWrapper might produce null result, because some residue listed in the species
                        does not exist in the referenced protein ex: e_p47 doesn't list residue d_rs1, but its species
                        has a residue d_rs1.
                         */
                        if(residueWrapper != null) {
                            residueWrapper.state = modif.getState().getStringValue();
                        }
                        else {
                            logger.error("Residue "+residueId+" doesn't exist in referenced protein.");
                        }
                    }




                /*else if(identity.isSetCelldesignerRnaReference()) {
                    String rnaId = identity.getCelldesignerRnaReference().getDomNode().getChildNodes().item(0).getNodeValue();
                    CelldesignerRNA rna = modelW.getRNA(rnaId);

                }
                else if(identity.isSetCelldesignerAntisensernaReference()) {
                    String asrnaId = identity.getCelldesignerAntisensernaReference().getDomNode().getChildNodes().item(0).getNodeValue();
                    CelldesignerAntisenseRNA asrna = modelW.getAntisenseRNA(asrnaId);

                }
                else if(identity.isSetCelldesignerGeneReference()) {
                    String geneId = identity.getCelldesignerGeneReference().getDomNode().getChildNodes().item(0).getNodeValue();
                    CelldesignerGeneDocument.CelldesignerGene gene = modelW.getGene(geneId);
                }*/


            }

        }
        // finally set this species' residue wrapper list
        this.residues.addAll(mapOfReferenceModif.values());
        System.out.println("final residue size for species "+this.getId()+" : "+this.residues.size());

    }

    public boolean isIncludedSpecies() {
        return isIncludedSpecies;
    }

    public boolean isComplex() {
        return isComplex;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompartment() {
        return compartment;
    }

    public String getComplex() {
        return complex;
    }

    public String getCdClass() {
        return cdClass;
    }

    public List<AliasWrapper> getAliases() {
        return aliases;
    }

    public int getMultimer() {
        return multimer;
    }

    public String getUnitOfInformation() {
        return unitOfInformation;
    }

    public List<ResidueWrapper> getResidues() {
        return residues;
    }

}
