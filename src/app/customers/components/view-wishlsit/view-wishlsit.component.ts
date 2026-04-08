import { Component } from '@angular/core';
import { CustomerService } from '../../services/customer.service';

@Component({
  selector: 'app-view-wishlsit',
  templateUrl: './view-wishlsit.component.html',
  styleUrls: ['./view-wishlsit.component.scss']
})
export class ViewWishlsitComponent {

  products: any[] = [];

  constructor( private customerService: CustomerService){}

  ngOnInit(){
    this.getWishlsitByUserId();
  }
  getWishlsitByUserId(){
    this.customerService.getWishlistByUserId().subscribe(res =>{
      res.forEach(element => {
        element.processedImg = 'data:image/jpeg;base64,' + element.returnedImg;
        this.products.push(element);
      })
    })
  }


}
