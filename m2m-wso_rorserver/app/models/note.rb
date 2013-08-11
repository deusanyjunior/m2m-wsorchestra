# -*- encoding : utf-8 -*-
class Note < ActiveRecord::Base
  attr_accessible :username, :note, :velocity, :created_at
  
  validates_presence_of :username, :note, :velocity
end
