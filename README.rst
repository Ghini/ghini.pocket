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

ghini.pocket has four pages: taxonomy review, search, results, and correction. 
  
- While in the search page:
  
  - scan a plant code (or type it),
  - the interface moves to the right, into the results page,
  - in the results page you see the most relevant details about the plant.

- While in the results page:

  - follow the link to the relative Wikipedia page,
  - swipe back to the search page,
  - swipe further back to the taxonomy review page,
  - swipe forward to the correction page.

- In the correction page:

  - you see the data as last exported from ghini.desktop,
  - two buttons also allow you:
    - grabbing your current coordinates,
    - taking pictures to be associated to the plant.
  - you click on the field you want to correct,
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

Speaking in terms of database interaction, getting into a view, that is
activating a `Fragment`, corresponds to begining a transaction.  There are
two ways to close a transaction, that is either aborting or committing it.
To swipe back corresponds to aborting the transaction while to activate the
`write` button corresponds to committing it.

The only `Fragment` which lets users provide data is the `CollectFragment`.
Consequently, this is the only `Fragment` that has a `confirm` button to it.

To handle this, `CollectFragment` has its own `state`, that is kept for as
long as the `Fragment` stays active, which is committed to the global
`state` upon confirmation, which is overwritten with the global `state` when
the `Fragment` becomes active again.

Wherever we have callbacks in the `Activity` that need read/write access to
the corresponding `Fragment` internal `state`, we have the `Fragment`
implement `FragmentWithState`, an interface that exposes the internal
`Fragment` `state` and offers an `updateView` method.  (Yes, it is
cumbersome, and yes, I prefer Python dynamic types, and I wish we could
define this all in the XSL gui definition file.)

After you confirm your edits, `ghini.pocket` will save them in logs and
local database, and it will show them as edited, but it will also remind you
that you are looking at data containing edits which are local to your
handheld device, meaning that what you now see does not necessarily match
the central database.  Please consider this as an alert: it is you task to
import the log file in `ghini.desktop`, and to again export the database to
`ghini.pocket`.

Pictures taken in the `CollectFragment` are saved when the user confirms the
edits, and are removed and lost together with all other edits if the user
swipes back to the `ResultFragment`, because all references to them are lost
when the edits are not persisted in the logs.

The global application `state` contains all that the user inserted in the
visible elements and that was manually persisted.  In this, it just reflects
the same contents as the `ghini.pocket` database
