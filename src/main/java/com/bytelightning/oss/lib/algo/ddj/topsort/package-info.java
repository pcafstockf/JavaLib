/**
 * Java implementation of a Dr Dobb's August 1997 article by Jonathan Pincus and Jerry Schwarz titled 'Topological Sorting'.
 * 	http://www.drdobbs.com/database/topological-sorting/184410262
 * 
 * This package supports dependency analysis where there may be cycles in the dependency. To quote from the article:
 * A topological sort uses a "partial order" -- you may know that A precedes both B and C, but not know (or care) whether B precedes C or C precedes B.
 * Topological sorting is a useful technique in many different domains, including software tools, dependency analysis, constraint analysis, and CAD.
 * Topological sorts can deal gracefully with cycles.
 * For example, imagine that two functions X and Y are mutually recursive: X calls Y and Y calls X.
 * In this case, it is useful to detect the cycle and the specific relations that cause the cycle.
 * Standard sorting algorithms, however, will simply fail in this situation.
 * We developed an extension to topological sorting that can produce a "best" order, even in the presence of cycles.
 * 	http://twimgs.com/ddj/ddj/images/ddj9708n/9708nf3.gif
 */
package com.bytelightning.oss.lib.algo.ddj.topsort;