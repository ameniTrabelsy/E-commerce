import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomersComponent } from './customers.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CartComponent } from './components/cart/cart.component';
import { MyOrdersComponent } from './components/my-orders/my-orders.component';
import { ViewOrderedProductsComponent } from './components/view-ordered-products/view-ordered-products.component';
import { ReviewOrderedProductComponent } from './components/review-ordered-product/review-ordered-product.component';
import { ViewProductDetailComponent } from './components/view-product-detail/view-product-detail.component';
import { ViewWishlsitComponent } from './components/view-wishlsit/view-wishlsit.component';

const routes: Routes = [
  { path: '', component: CustomersComponent },
  { path:'dashboard', component: DashboardComponent},
  { path:'cart', component: CartComponent},
  { path:'my_orders', component: MyOrdersComponent},
  { path: 'ordered_products/:orderId', component: ViewOrderedProductsComponent},
  { path: 'review/:productId', component: ReviewOrderedProductComponent},
  { path: 'product/:productId', component: ViewProductDetailComponent},
  { path: 'wishlist', component:ViewWishlsitComponent},


];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CustomersRoutingModule { }
