/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */

package importers

import restclient.CategoryServiceConnection
import restclient.MediaRepositoryServiceConnection


class CategoryImport {


    def categories = '';
    def categoryServiceConnection = '';
    def categoryUploadImageServiceConnection = '';

    def CategoryImport(baseurl, tenant, access_token, app_identifier) {
        categoryServiceConnection = new CategoryServiceConnection(baseurl, tenant, access_token);
        categoryUploadImageServiceConnection = new MediaRepositoryServiceConnection(baseurl, tenant, access_token, app_identifier);
        
        this.categories = loadCategories()
    }


    def createOrUpdateCategory(newCategory, folder) {

        def newCatImage = newCategory.image;
        newCategory.remove("image");

        def category = findCategory(newCategory.name.en)
        def media = null;

        if (category == null) {

         //Upload image
            if (newCatImage && !"".equals(newCatImage.trim()) && !newCatImage.trim().endsWith('/')) {
                def imageFile = new File(new File(folder, "category-images"), newCatImage)
                if (imageFile.exists()) {

                    println 'Uploading media';

                     media = categoryUploadImageServiceConnection.createCategoryMediaFromFile(imageFile.getPath());
                    if (media != null) {
                        newCategory.image = [en: media.link];
                    }
                }
            }
            if (newCategory.parent) {
                def parent = findCategory(newCategory.parent)
                if (parent) {
                    newCategory.parentId = parent.id
                } 
            }

            newCategory.remove('parent');

            println "creating category ${newCategory}"

            categoryServiceConnection.createCategory(newCategory)
            this.categories = loadCategories() //refresh categories after creating a new one
        } else {
            def nameStr = "{\"en\": \"${newCategory.name.en}\", \"de\": \"${newCategory.name.de}\"}"
            def categoryUpdate = [name: newCategory.name]

            if (category.image != null && category.image == newCatImage) {
                println 'Image found, not re-uploading.';   
            }
            else{
             //Upload image
                if (newCatImage && !"".equals(newCatImage.trim()) && !newCatImage.trim().endsWith('/')) {
                    def imageFile = new File(new File(folder, "category-images"), newCatImage)
                    if (imageFile.exists()) {

                        println 'Uploading media';

                         media = categoryUploadImageServiceConnection.createCategoryMediaFromFile(imageFile.getPath());
                        if (media != null) {
                            categoryUpdate.image = [en: media.link];
                        }
                    }
                }
            }            

            if (newCategory.parent) {
                def parent = findCategory(newCategory.parent)
                if (parent) {
                    categoryUpdate.parentId = parent.id
                }
            }
            println "updating category ${category.id} with ${categoryUpdate}"
            categoryServiceConnection.updateCategory(category.id, categoryUpdate)
        }


    }

    def findCategory(name) {
        for (c in this.categories) {
            if (name.equals(c.name)) {
                return c
            }
        }
    }

    def deleteCategory(name){
        println "deleting category ${name}"
           
        def cat = findCategory(name);
        if (cat != null && cat.id != null) {
            categoryServiceConnection.deleteCategory(cat.id);
        }
        else{
            println "category ${name} not found"
        }
    }

    def loadCategories() {
        return categoryServiceConnection.getCategories("subcategories");
    }

    def getCategories() {
        return this.categories
    }

    def assignProductToCategory(category, productId) {
        boolean categoryContainsProduct = false
        for (element in category.elements) {
            if (element.ref.id == productId && element.ref.type == "product") {
                categoryContainsProduct = true
                break
            }
        }
        if (!categoryContainsProduct) {
            println "assinging product ${productId} to category ${category.name} (id: ${category.id})"
            categoryServiceConnection.addProductToCategory(productId, category.id)
        }
    }
}