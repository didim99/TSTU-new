package ru.didim99.tstu.core.graphics;

import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.ui.view.DrawerView;

/**
 * Created by didim99 on 13.12.19.
 */
public class FractalRenderer extends ModelRenderer {

  public FractalRenderer(DrawerView target, Config config) {
    super(Model.Type.EDGE, target, config);
  }

}
