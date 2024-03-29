/**
 * JDBM LICENSE v1.00
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "JDBM" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Cees de Groot.  For written permission,
 *    please contact cg@cdegroot.com.
 *
 * 4. Products derived from this Software may not be called "JDBM"
 *    nor may "JDBM" appear in their names without prior written
 *    permission of Cees de Groot.
 *
 * 5. Due credit should be given to the JDBM Project
 *    (http://jdbm.sourceforge.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2000 (C) Cees de Groot. All Rights Reserved.
 * Contributions are Copyright (C) 2000 by their associated contributors.
 *
 * $Id: FreePhysicalRowIdPageManager.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

package jdbm.recman;

import java.io.IOException;

/**
 *  This class manages free physical rowid pages and provides methods
 *  to free and allocate physical rowids on a high level.
 */
final class FreePhysicalRowIdPageManager
{
    // our record file
    protected RecordFile _file;

    // our page manager
    protected PageManager _pageman;

    /**
     *  Creates a new instance using the indicated record file and
     *  page manager.
     */
    FreePhysicalRowIdPageManager( RecordFile file, PageManager pageman )
        throws IOException
    {
        _file = file;
        _pageman = pageman;
    }


    /**
	 * Returns a free physical rowid of the indicated size, or null if nothing
	 * was found. This scans the free physical row table, which is modeled as a
	 * linked list of pages. Each page on that list has slots that are either
	 * free (awaiting the insertion of the location of a free physical row) or
	 * available for reallocation requests. An allocated slot is indicated by a
	 * non-zero size field in that slot and the size is the size of the
	 * available free record in bytes.
	 */
    Location get( int size )
        throws IOException
    {
        // Loop through the free physical rowid list until we find
        // a rowid that's large enough.
        Location retval = null;
        PageCursor curs = new PageCursor( _pageman, Magic.FREEPHYSIDS_PAGE );

        while (curs.next() != 0) {
            FreePhysicalRowIdPage fp = FreePhysicalRowIdPage
                .getFreePhysicalRowIdPageView( _file.get( curs.getCurrent() ) );
            int slot = fp.getFirstLargerThan( size );
            if ( slot != -1 ) {
                // got one!
                retval = new Location( fp.get( slot ) );

                fp.free( slot );
                if ( fp.getCount() == 0 ) {
                    // page became empty - free it
                    _file.release( curs.getCurrent(), false );
                    _pageman.free( Magic.FREEPHYSIDS_PAGE, curs.getCurrent() );
                } else {
                    _file.release( curs.getCurrent(), true );
                }

                return retval;
            } else {
                // no luck, go to next page
                _file.release( curs.getCurrent(), false );
            }

        }
        return null;
    }

    /**
     *  Puts the indicated rowid on the free list
     */
    void put(Location rowid, int size)
  throws IOException {

  FreePhysicalRowId free = null;
  PageCursor curs = new PageCursor(_pageman, Magic.FREEPHYSIDS_PAGE);
  long freePage = 0;
  while (curs.next() != 0) {
      freePage = curs.getCurrent();
      BlockIo curBlock = _file.get(freePage);
      FreePhysicalRowIdPage fp = FreePhysicalRowIdPage
    .getFreePhysicalRowIdPageView(curBlock);
      int slot = fp.getFirstFree();
      if (slot != -1) {
    free = fp.alloc(slot);
    break;
      }

      _file.release(curBlock);
  }
  if (free == null) {
      // No more space on the free list, add a page.
      freePage = _pageman.allocate(Magic.FREEPHYSIDS_PAGE);
      BlockIo curBlock = _file.get(freePage);
      FreePhysicalRowIdPage fp =
    FreePhysicalRowIdPage.getFreePhysicalRowIdPageView(curBlock);
      free = fp.alloc(0);
  }

  free.setBlock(rowid.getBlock());
  free.setOffset(rowid.getOffset());
  free.setSize(size);
  _file.release(freePage, true);
    }
}
