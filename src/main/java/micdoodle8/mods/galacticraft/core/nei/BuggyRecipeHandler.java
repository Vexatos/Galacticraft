package micdoodle8.mods.galacticraft.core.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BuggyRecipeHandler extends TemplateRecipeHandler
{
	private static final ResourceLocation buggyGuiTexture = new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/gui/buggybench.png");

	public String getRecipeId()
	{
		return "galacticraft.buggy";
	}

	@Override
	public int recipiesPerPage()
	{
		return 1;
	}

	public Set<Entry<ArrayList<PositionedStack>, PositionedStack>> getRecipes()
	{
		HashMap<ArrayList<PositionedStack>, PositionedStack> recipes = new HashMap<ArrayList<PositionedStack>, PositionedStack>();

		for (Entry<HashMap<Integer, PositionedStack>, PositionedStack> stack : NEIGalacticraftConfig.getBuggyBenchRecipes())
		{
			ArrayList<PositionedStack> inputStacks = new ArrayList<PositionedStack>();

			for (Map.Entry<Integer, PositionedStack> input : stack.getKey().entrySet())
			{
				inputStacks.add(input.getValue());
			}

			recipes.put(inputStacks, stack.getValue());
		}

		return recipes.entrySet();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GuiDraw.changeTexture(BuggyRecipeHandler.buggyGuiTexture);
		GuiDraw.drawTexturedModalRect(0, 0, 3, 4, 168, 130);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
	}

	@Override
	public void loadTransferRects()
	{
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if (outputId.equals(this.getRecipeId()))
		{
			for (final Map.Entry<ArrayList<PositionedStack>, PositionedStack> irecipe : this.getRecipes())
			{
				this.arecipes.add(new CachedRocketRecipe(irecipe));
			}
		}
		else
		{
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		for (final Map.Entry<ArrayList<PositionedStack>, PositionedStack> irecipe : this.getRecipes())
		{
			if (NEIServerUtils.areStacksSameTypeCrafting(irecipe.getValue().item, result))
			{
				this.arecipes.add(new CachedRocketRecipe(irecipe));
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for (final Map.Entry<ArrayList<PositionedStack>, PositionedStack> irecipe : this.getRecipes())
		{
			for (final PositionedStack pstack : irecipe.getKey())
			{
				if (NEIServerUtils.areStacksSameTypeCrafting(ingredient, pstack.item))
				{
					this.arecipes.add(new CachedRocketRecipe(irecipe));
					break;
				}
			}
		}
	}

	public class CachedRocketRecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public ArrayList<PositionedStack> input;
		public PositionedStack output;

		@Override
		public ArrayList<PositionedStack> getIngredients()
		{
			return this.input;
		}

		@Override
		public PositionedStack getResult()
		{
			return this.output;
		}

		public CachedRocketRecipe(ArrayList<PositionedStack> pstack1, PositionedStack pstack2)
		{
			super();
			this.input = pstack1;
			this.output = pstack2;
		}

		public CachedRocketRecipe(Map.Entry<ArrayList<PositionedStack>, PositionedStack> recipe)
		{
			this(recipe.getKey(), recipe.getValue());
		}
	}

	@Override
	public String getRecipeName()
	{
		return "NASA Workbench";
	}

	@Override
	public String getGuiTexture()
	{
        return GalacticraftCore.TEXTURE_PREFIX + "textures/gui/buggybench.png";
	}
}
