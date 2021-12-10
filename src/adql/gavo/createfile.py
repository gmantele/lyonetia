"""
A little script to make creating query example files easier.

This is lean and mean.  Don't complain if it crashes on you.
"""

import os
import sys
import tkinter
import uuid
from xml.etree import ElementTree as etree


CONTACT_NAME = "Markus Demleitner"
CONTACT_URL = "mailto:msdemlei@ari.uni-heidelberg.de"
PUBLISHER_NAME = "Heidelberg GAVO Data Center"
PUBLISHER_URL = "http://dc.g-vo.org"
DEST_VERSION = "adql-2.1"

CONSTANT_PREFIX = "select x from y where "
CONSTANT_PREFIX = ""


############ tiny DOM start (snarfed and simplified from stanxml)

class _Element(object):
  """An element within a DOM.

  Essentially, this is a simple way to build elementtrees.  You can
  reach the embedded elementtree Element as node.

  Add elements, sequences, etc, using indexation, attributes using function
  calls; names with dashes are written with underscores, python
  reserved words have a trailing underscore.
  """
  _generator_t = type((x for x in ()))

  def __init__(self, name):
      self.node = etree.Element(name)

  @classmethod
  def from_element(cls, node):
    """creates an _Element from an elementtree Element.
    """
    self = cls(node.tag)
    self.node = node
    return self

  def add_text(self, tx):
      """appends tx either the end of the current content.
      """
      if len(self.node):
          self.node[-1].tail = (self.node[-1] or "")+tx
      else:
          self.node.text = (self.node.text or "")+tx

  def __getitem__(self, child):
      if child is None:
          return

      elif isinstance(child, str):
          self.add_text(child)

      elif isinstance(child, (int, float)):
          self.add_text(str(child))

      elif isinstance(child, _Element):
          self.node.append(child.node)

      elif isinstance(child, (list, tuple, self._generator_t)):
          for c in child:
              self[c]
      else:
          raise Exception("%s element %s cannot be added to %s node"%(
              type(child), repr(child), self.node.tag))
      return self
  
  def __call__(self, **kwargs):
      for k, v in kwargs.items():
          if k.endswith("_"):
              k = k[:-1]
          k = k.replace("_", "-")
          self.node.attrib[k] = v
      return self

  def dump(self, encoding="utf-8", dest_file=sys.stdout):
    etree.ElementTree(self.node).write(dest_file)


class _T(object):
    """a very simple templating engine.

    Essentially, you get HTML elements by saying T.elementname, and
    you'll get an _Element with that tag name.

    This is supposed to be instanciated to a singleton (here, T).
    """
    def __getattr__(self, key):
        return  _Element(key)

T = _T()

############ tiny DOM end


class UI(tkinter.Tk):
  def __init__(self, dest_doc):
    self.dest_doc = dest_doc
    tkinter.Tk.__init__(self)
    self._make_widgets()
  
  def _make_widgets(self):
    tkinter.Label(self, text="Description").grid(column=1, row=1,
      sticky=tkinter.NW+tkinter.SE)
    self.desc_entry = tkinter.Entry(self)
    self.desc_entry.grid(column=2, row=1,
      sticky=tkinter.NW+tkinter.SE)

    self.is_valid = tkinter.IntVar()
    self.is_valid.set(1)
    valid_box = tkinter.Checkbutton(self, text="valid", var=self.is_valid)
    valid_box.grid(column=3, row=1,
      sticky=tkinter.NW+tkinter.SE)

    self.query_box = tkinter.Text(self)
    self.query_box.insert(tkinter.END, CONSTANT_PREFIX)
    self.query_box.grid(column=1, row=2, columnspan=3,
      sticky=tkinter.NW+tkinter.SE)
  
    tkinter.Label(self, text="^Q quit  ^Space save entry"
      ).grid(column=1, row=3, sticky=tkinter.NW+tkinter.SE)
    self.columnconfigure(2, weight=1)
    self.rowconfigure(2, weight=1)

    self.bind("<Control-q>", lambda ev: self.quit())
    self.bind("<Control-space>", self.shipout)

  def shipout(self, ev):
    self.dest_doc[T.query(uuid=str(uuid.uuid1()))[
      T.description[self.desc_entry.get()],
      T.adql(version=DEST_VERSION, 
          valid=(self.is_valid.get() and 'true') or 'false')[
        self.query_box.get(1.0, tkinter.END)]]]
    self.query_box.delete(1.0, tkinter.END)
    self.query_box.insert(tkinter.END, CONSTANT_PREFIX)


def main():
  if len(sys.argv)!=2:
    sys.exit("Usage: %s <destination file name>"%sys.argv[0])
  dest_file_name = sys.argv[1]

  if os.path.exists(dest_file_name):
    with open(dest_file_name, encoding="utf-8") as f:
      doc = _Element.from_element(etree.parse(f).getroot())
  else:
    doc = T.queries[
      T.contact[
        T.name[CONTACT_NAME],
        T.url[CONTACT_URL]
      ],
      T.publisher[
        T.name[PUBLISHER_NAME],
        T.url[PUBLISHER_URL],
      ],
      T.description["MISSING: Global description of these queries"]]

  ui = UI(doc)
  ui.mainloop()
  with open(dest_file_name, "wb") as dest_file:
    doc.dump(dest_file=dest_file)


if __name__=="__main__":
  main()

# vim:set et:sw=2:sta
