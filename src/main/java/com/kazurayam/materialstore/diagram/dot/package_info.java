/**
 * This module is generates diagrams of instances of
 * Material, MaterialList, MaterialProduct and MProductGroup classes.
 *
 * <p>MPGVisualizer is designed to be used by test scripts.</p>
 *
 * <p>MPGVisualiser calls DotGenerator, which actually generate dot scripts and generates PNG images using Graphviz.</p>
 *
 * <p>
 * The rule of node names:
 *
 * - a stand alone Material object :
 *         "Mxxxxxxx" where "xxxxxxx" is the ShortId of the Material object.
 *
 * - a Material object contained a MaterialList objectct :
 *         "MLyyyyyyy_Mxxxxxxx" where "yyyyyyy" is the ShortId of the MaterialList object,
 *         "xxxxxxx" is the ShortId of the Material object.
 *
 * - a Material object contained in a MaterialProduct object:
 *         - "MPzzzzzzz_MxxxxxxxL"
 *         - "MPzzzzzzz_MxxxxxxxR"
 *         "where "zzzzzzz" is the ShortId of the MaterProduct object,
 *         "xxxxxxx" is the ShoftId of the Material object.
 *         "L" is short for "Left", and "R" short for the "Right".
 *
 * - a QueryOnMetadata object contained in a MaterialProduct object:
 *         "MPzzzzzzz_Q"
 *         where "zzzzzzz" is the ShortId of the MaterialProduct object.
 *
 * - a Material object as Diff contained in a MaterialProduct Object:
 *         "MPzzzzzzz_D"
 *         where "zzzzzzz" is the ShortId of the MaterialProduct Object:
 * </p>
 *
 * @since 0.8.5
 * @author kazurayam
 * @version 0.8.5
 */
package com.kazurayam.materialstore.diagram.dot;