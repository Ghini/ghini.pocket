ghini.pocket
============================

ghini.pocket is a tiny android data viewer. you would find it handy if you
want to have a quick idea of a plant species, origin, and date it entered
the garden, just by scanning a plant label.

- Install `ghini.pocket <https://play.google.com/store/apps/details?id=me.ghini.pocket>`_ on your android device,
- Start ghini.pocket,
- Search for plant code ``0`` (this creates the database location),
- Search for plant code ``settings`` (this tells you the database location),

- Use **ghini.desktop** to export your garden database to a reduced SQLite3 database,
- Copy the reduced database file to the device (this and the previous step you repeat from time to time),

ghini.pocket has four pages: taxonomy review, search, results, and correction (not implemented yet).  When you 
  
- While in the search page:
  
  - scan a plant code (or type it),
  - the interface moves to the right, into the results page,
  - in the results page you see the most relevant details about the plant.

- While in the results page:

  - follow the link to the relative Wikipedia page,
  - swipe back to the search page,
  - swipe further back to the taxonomy review page,
  - swipe forward to the correction page (not implemented yet).

- In the correction page (not implemented yet):

  - you see the data as last exported from ghini.desktop,
  - two buttons also allow you:
    - grabbing your current coordinates,
    - taking a picture and associate it to the plant.
  - you click and hold on the field you want to correct,
  - you type the new value,
  - you confirm your edits (which get logged),
  - or you swipe back (and abort your edits).

- In the taxonomy review page:

  - type a genus name, or its exact initial letters,
  - start the search,
  - the result is the path from genus up to order,
  - multiple matches are possible.
    
  - type the search item all capitals, and the search will be phonetic.

  - if no genus was found, the search is performed among tribes, then families, then orders.
  
==================================== ==================================== ====================================
.. image:: images/ghini.pocket-0.png .. image:: images/ghini.pocket-1.png .. image:: images/ghini.pocket-2.png
==================================== ==================================== ==================================== 

the ghini family
-----------------

and the place of ghini.pocket within the family

.. image:: images/ghini-family.png


Technical Information
-----------------------------------

The program is written in Java, and only runs on Android.

There is one `Activity` and several `Fragments` (at the time of writing it's
four `Fragments`: taxonomy, search, results, collect).

`Fragments` have the role to manage the view elements, while the `Activity`
implements callbacks, manages the internal `state`, passes the internal
`state` to `Fragments` upon activation.

Users staying in the first three `Fragments`, need not worry about
synchronizing with the database.  Just consider `ghini.pocket` as a *read
only* application, periodically export from `ghini.desktop` to
`ghini.pocket` and you will be fine.

However, `ghini.pocket` can also be used to collect data to be imported into
the `ghini.desktop` database.  This is the role of the `CollectFragment`.

Consider that anything written in view elements isn't automatically
persisted, so when the user swipes left or right to leave a `Fragment`, all
that was written in the `Fragment` visible elements is lost and will be
recovered from the database, according to the last searched plant code.

The only persistent data written in a visible element is the searched plant
code.

Users can make the data written in the `CollectFragment` form manually
persistent, by writing the values to the log file and into the
`ghini.pocket` database.  Once your edits have been saved, `ghini.pocket`
will show them, but it will also remind you that you are looking at locally
edited data, meaning that what you now see does not necessarily match the
central database.  Users should consider this as an alert: it is their task
to import the log file in `ghini.desktop`, and to again export the database
to `ghini.pocket`.

The internal `state` contains all that the user inserted in the visible
elements and that was manually persisted.  In this, it just reflects the
same contents as the `ghini.pocket` database
