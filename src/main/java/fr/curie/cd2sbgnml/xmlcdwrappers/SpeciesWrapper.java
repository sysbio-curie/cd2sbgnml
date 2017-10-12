package fr.curie.cd2sbgnml.xmlcdwrappers;

import fr.curie.cd2sbgnml.graphics.CdShape;
import org.sbml.x2001.ns.celldesigner.CelldesignerClassDocument.CelldesignerClass;
import org.sbml.x2001.ns.celldesigner.CelldesignerComplexSpeciesAliasDocument.CelldesignerComplexSpeciesAlias;
import org.sbml.x2001.ns.celldesigner.CelldesignerComplexSpeciesDocument.CelldesignerComplexSpecies;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesAliasDocument.CelldesignerSpeciesAlias;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesDocument.CelldesignerSpecies;
import org.sbml.x2001.ns.celldesigner.CelldesignerStateDocument;
import org.sbml.x2001.ns.celldesigner.CelldesignerStateDocument.CelldesignerState;
import org.sbml.x2001.ns.celldesigner.SpeciesDocument.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    public SpeciesWrapper(Species species, ModelWrapper modelW) {
        this.isIncludedSpecies = false;
        CelldesignerClass cdClassClass = species.getAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerClass();
        this.cdClass = cdClassClass.getDomNode().getChildNodes().item(0).getNodeValue();
        this.isComplex = this.cdClass.equals("COMPLEX");
        this.id = species.getId();
        this.name = species.getName().getStringValue();
        this.compartment = species.getCompartment();
        this.complex = null;
        this.aliases = new ArrayList<>();

        if(this.isComplex) {
            for(CelldesignerComplexSpeciesAlias complexAlias : modelW.getComplexSpeciesAliasFor(this.id)) {
                if (complexAlias == null) {
                    continue;
                }
                this.aliases.add(new AliasWrapper(complexAlias, this));
            }
        }
        //else {
        /**
         * here we shouldn't have to check normal aliases after complex aliases.
         * normally complex species should only have complex aliases, and not additional normal aliases
         * this only happens in ACSN
         */
        if(modelW.getSpeciesAliasFor(this.id) != null) {
            if(this.isComplex) {
                logger.warn("Complex species: "+this.id+" shouldn't have non-complex aliases");
            }

            for(CelldesignerSpeciesAlias alias : modelW.getSpeciesAliasFor(this.id)) {
                if (alias == null) {
                    continue;
                }
                this.aliases.add(new AliasWrapper(alias, this));
            }
        }

        // parse multimer and infounit
        this.multimer = 1; // default to 1 if nothing else found
        if(species.getAnnotation().getCelldesignerSpeciesIdentity().isSetCelldesignerState()) {
            CelldesignerState state = species.getAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerState();
            if(state.isSetCelldesignerHomodimer()) {
                this.multimer = Integer.parseInt(state.getCelldesignerHomodimer().
                        getDomNode().getChildNodes().item(0).getNodeValue());
            }

            if(state.isSetCelldesignerListOfStructuralStates()) {
                // assume that there is only 1 state per species
                this.unitOfInformation = state.getCelldesignerListOfStructuralStates().
                        getCelldesignerStructuralStateArray(0).getStructuralState().getStringValue();
            }
        }

    }

    public SpeciesWrapper(CelldesignerSpecies species, ModelWrapper modelW) {
        this.isIncludedSpecies = true;
        CelldesignerClass cdClassClass = species.getCelldesignerAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerClass();
        this.cdClass = cdClassClass.getDomNode().getChildNodes().item(0).getNodeValue();
        this.isComplex = this.cdClass.equals("COMPLEX");
        this.id = species.getId();
        this.name = species.getName().getStringValue();
        CelldesignerComplexSpecies complexSpecies = species.getCelldesignerAnnotation().getCelldesignerComplexSpecies();
        this.complex = complexSpecies.getDomNode().getChildNodes().item(0).getNodeValue();
        this.compartment = null;
        this.aliases = new ArrayList<>();

        if(this.isComplex) {
            for(CelldesignerComplexSpeciesAlias complexAlias : modelW.getComplexSpeciesAliasFor(this.id)) {
                if (complexAlias == null) {
                    continue;
                }
                this.aliases.add(new AliasWrapper(complexAlias, this));
            }
        }
        //else {
        /**
         * here we shouldn't have to check normal aliases after complex aliases.
         * normally complex species should only have complex aliases, and not additional normal aliases
         * this only happens in ACSN
         */
        if(modelW.getSpeciesAliasFor(this.id) != null) {
            if(this.isComplex) {
                logger.warn("Included complex species: "+this.id+" shouldn't have non-complex aliases");
            }

            for(CelldesignerSpeciesAlias alias : modelW.getSpeciesAliasFor(this.id)) {
                if (alias == null) {
                    continue;
                }
                this.aliases.add(new AliasWrapper(alias, this));
            }
        }
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

}
